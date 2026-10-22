# CP3 - How to run

Module `workflow-tracing`: a minimal `opti-workflow` in Java 21 (same stack as the real service)
with no framework, so it runs anywhere with a JDK and Maven.

| Class | What it does |
|---|---|
| `TraceContext` | parses or creates the W3C `traceparent`, keeps `X-Correlation-Id`, makes child spans |
| `JsonLog` | writes the log contract of `02-log-contract.md` with `trace_id` and `span_id` injected |
| `TracingFilter` | inbound filter: reads/creates the context, returns it in the response, one access line |
| `PatientsClient` | step `check-patient`: `GET /api/v1/patients/{id}` on customers-api with a child `traceparent` |
| `WorkflowMetrics` | `workflow_sagas_total` (counter by outcome) and `workflow_http_request_duration_seconds` (histogram) |
| `WorkflowServer` | `POST /api/v1/sagas/place-order` and `GET /metrics` |
| `TracePropagationTest` | proves the `trace_id` that arrives from the gateway is the one sent to customers-api |

In the real `opti-workflow` the same code goes in `CorrelationFilter` (inbound) and in
`ParticipantClient.build(...)` (outbound), next to the `X-Correlation-Id` header it already sets.

## Requirements

- JDK 21 (`java -version`)
- Maven 3.9 (`mvn -v`)

## Test

```bash
cd 11-week/workshop/03-instrumentation/workflow-tracing
mvn -B test
```

## Run by hand

```bash
mvn -B -q package -DskipTests
CUSTOMERS_API_URL=http://localhost:8081 java -cp target/classes co.edu.corhuila.opti.workflow.observability.WorkflowServer
# other terminal
curl -i -X POST http://localhost:8080/api/v1/sagas/place-order \
  -H "traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01" \
  -d '{"patientId":"6f1c2a9e-3b7d-4c51-9a0e-2d8f4b6c1e77"}'
curl http://localhost:8080/metrics
```

Without a customers-api on port 8081 the call fails fast and the saga is counted as `failed`,
which is also useful to see the `downstream.unavailable` log line.

## Console output on my machine

```
[INFO] Running co.edu.corhuila.opti.workflow.observability.TracePropagationTest
{"timestamp":"2026-10-22T03:41:58.495985610Z","level":"INFO","service":"opti-workflow","version":"2.0.0","environment":"test","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"efe627c25df1acbf","correlation_id":"corr-ot-0142","event":"saga.started","message":"place-order saga started","duration_ms":0,"saga_type":"place-order","patient_id":"6f1c2a9e-3b7d-4c51-9a0e-2d8f4b6c1e77"}
{"timestamp":"2026-10-22T03:41:58.569798623Z","level":"INFO","service":"opti-workflow","version":"2.0.0","environment":"test","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"efe627c25df1acbf","correlation_id":"corr-ot-0142","event":"saga.step.completed","message":"customers-api answered 200","duration_ms":72,"saga_step":"check-patient","downstream":"opti-customers-api","http_status":200,"patient_id":"6f1c2a9e-3b7d-4c51-9a0e-2d8f4b6c1e77"}
{"timestamp":"2026-10-22T03:41:58.572221630Z","level":"INFO","service":"opti-workflow","version":"2.0.0","environment":"test","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"efe627c25df1acbf","correlation_id":"corr-ot-0142","event":"http.request.completed","message":"POST /api/v1/sagas/place-order","duration_ms":76,"route":"/api/v1/sagas/place-order","http_status":201}
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.620 s -- in co.edu.corhuila.opti.workflow.observability.TracePropagationTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
