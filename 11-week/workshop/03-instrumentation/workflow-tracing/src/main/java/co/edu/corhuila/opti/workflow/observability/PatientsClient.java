package co.edu.corhuila.opti.workflow.observability;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;

/**
 * Outbound side of the check-patient step: GET /api/v1/patients/{id} on opti-customers-api with a
 * child span in traceparent and the same X-Correlation-Id, so customers-api logs land in the trace.
 */
public class PatientsClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;
    private final Duration timeout;
    private final JsonLog log;

    public PatientsClient(String baseUrl, Duration timeout, JsonLog log) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.log = log;
    }

    /** Returns the HTTP status of customers-api; a timeout is reported as 504. */
    public int checkPatient(String patientId) throws InterruptedException {
        TraceContext span = TraceContext.current().child();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/patients/" + patientId))
                .timeout(timeout)
                .header("traceparent", span.traceparent())
                .header("X-Correlation-Id", span.correlationId())
                .GET().build();
        long started = System.nanoTime();
        try {
            int status = http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            log.info("saga.step.completed", "customers-api answered " + status, elapsed(started),
                    Map.of("saga_step", "check-patient", "downstream", "opti-customers-api",
                            "patient_id", patientId, "http_status", status));
            return status;
        } catch (HttpTimeoutException e) {
            log.error("downstream.timeout", "customers-api did not answer in time", elapsed(started),
                    Map.of("saga_step", "check-patient", "downstream", "opti-customers-api", "patient_id", patientId));
            return 504;
        } catch (IOException e) {
            log.error("downstream.unavailable", "no response from customers-api", elapsed(started),
                    Map.of("saga_step", "check-patient", "downstream", "opti-customers-api", "patient_id", patientId));
            return 503;
        }
    }

    private static long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }
}
