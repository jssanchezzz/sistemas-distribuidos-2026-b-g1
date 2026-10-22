package co.edu.corhuila.opti.workflow.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class TracePropagationTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String GATEWAY_SPAN = "00f067aa0ba902b7";
    private static final String PATIENT = "6f1c2a9e-3b7d-4c51-9a0e-2d8f4b6c1e77";

    private final AtomicReference<String> seenByCustomers = new AtomicReference<>();
    private final AtomicReference<String> correlationSeenByCustomers = new AtomicReference<>();
    private final List<String> logLines = new CopyOnWriteArrayList<>();
    private HttpServer customersApi;
    private WorkflowServer workflow;

    @BeforeEach
    void start() throws Exception {
        customersApi = HttpServer.create(new InetSocketAddress(0), 0);
        customersApi.createContext("/api/v1/patients/", exchange -> {
            seenByCustomers.set(exchange.getRequestHeaders().getFirst("traceparent"));
            correlationSeenByCustomers.set(exchange.getRequestHeaders().getFirst("X-Correlation-Id"));
            WorkflowServer.send(exchange, 200, "{\"status\":\"ACTIVE\"}");
        });
        customersApi.start();
        JsonLog log = new JsonLog("opti-workflow", "2.0.0", "test", line -> {
            logLines.add(line);
            System.out.println(line);
        });
        workflow = new WorkflowServer(0, "http://localhost:" + customersApi.getAddress().getPort(), log,
                new WorkflowMetrics());
        workflow.start();
    }

    @AfterEach
    void stop() {
        workflow.stop();
        customersApi.stop(0);
    }

    @Test
    void traceIdReceivedFromTheGatewayIsTheOneSentToCustomersApi() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(
                        URI.create("http://localhost:" + workflow.port() + "/api/v1/sagas/place-order"))
                .header("traceparent", "00-" + TRACE_ID + "-" + GATEWAY_SPAN + "-01")
                .header("X-Correlation-Id", "corr-ot-0142")
                .POST(HttpRequest.BodyPublishers.ofString("{\"patientId\":\"" + PATIENT + "\",\"productType\":\"FRAME\"}"))
                .build(), HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        String[] downstream = seenByCustomers.get().split("-");
        assertEquals(TRACE_ID, downstream[1], "same trace_id downstream");
        assertNotEquals(GATEWAY_SPAN, downstream[2], "customers-api gets a new child span");
        assertEquals("corr-ot-0142", correlationSeenByCustomers.get());
        assertTrue(response.headers().firstValue("traceparent").orElseThrow().contains(TRACE_ID));
        assertTrue(logLines.stream().allMatch(line -> line.contains("\"trace_id\":\"" + TRACE_ID + "\"")));
        assertTrue(logLines.stream().noneMatch(line -> line.contains("fullName") || line.contains("document_number")));
    }

    @Test
    void metricsCountTheSagaAndItsLatency() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + workflow.port() + "/api/v1/sagas/place-order"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"patientId\":\"" + PATIENT + "\"}")).build(),
                HttpResponse.BodyHandlers.discarding());
        String metrics = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + workflow.port() + "/metrics"))
                .build(), HttpResponse.BodyHandlers.ofString()).body();

        assertTrue(metrics.contains("workflow_sagas_total{saga_type=\"place-order\",outcome=\"completed\"} 1"));
        assertTrue(metrics.contains("workflow_http_request_duration_seconds_count{route=\"/api/v1/sagas/place-order\",method=\"POST\"} 1"));
    }
}
