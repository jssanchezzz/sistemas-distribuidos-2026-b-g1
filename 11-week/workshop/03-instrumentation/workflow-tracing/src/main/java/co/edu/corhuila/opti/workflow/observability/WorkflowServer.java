package co.edu.corhuila.opti.workflow.observability;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Minimal opti-workflow: POST /api/v1/sagas/place-order runs only the check-patient step (enough to
 * show the propagation) and GET /metrics exposes WorkflowMetrics.
 */
public class WorkflowServer {

    private static final Pattern PATIENT_ID = Pattern.compile("\"patientId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    private final HttpServer server;

    public WorkflowServer(int port, String customersUrl, JsonLog log, WorkflowMetrics metrics) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        PatientsClient patients = new PatientsClient(customersUrl, Duration.ofSeconds(5), log);
        var placeOrder = server.createContext("/api/v1/sagas/place-order", exchange -> {
            long started = System.nanoTime();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Matcher match = PATIENT_ID.matcher(body);
            if (!match.find()) {
                send(exchange, 400, "{\"error\":\"VALIDATION_ERROR\",\"message\":\"patientId is required\"}");
                return;
            }
            log.info("saga.started", "place-order saga started", 0, Map.of("saga_type", "place-order",
                    "patient_id", match.group(1)));
            try {
                int status = patients.checkPatient(match.group(1));
                String outcome = status == 200 ? "completed" : status == 404 ? "compensated" : "failed";
                metrics.sagaFinished(outcome);
                send(exchange, 201, "{\"status\":\"" + outcome.toUpperCase() + "\"}");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                send(exchange, 503, "{\"error\":\"SERVICE_UNAVAILABLE\"}");
            } finally {
                metrics.requestDuration((System.nanoTime() - started) / 1e9);
            }
        });
        placeOrder.getFilters().add(new TracingFilter(log));
        server.createContext("/metrics", exchange -> send(exchange, 200, metrics.scrape()));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (var out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    public static void main(String[] args) throws IOException {
        String customers = System.getenv().getOrDefault("CUSTOMERS_API_URL", "http://localhost:8081");
        var server = new WorkflowServer(8080, customers, new JsonLog("opti-workflow", "2.0.0", "local",
                System.out::println), new WorkflowMetrics());
        server.start();
    }
}
