# CP4 - Golden signals and metric catalog

Names follow Prometheus rules: `snake_case`, prefix = service, unit suffix (`_seconds`), `_total`
for counters. The Spring services would expose them with Micrometer on `/actuator/prometheus`
(today `management.endpoints.enabled-by-default: false`, so it must be turned on); the gateway
needs `nginx-prometheus-exporter` or metrics derived from its JSON access log. The two
`opti-workflow` metrics already exist in `03-instrumentation/workflow-tracing`.

## opti-api-gateway (nginx)

| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | `gateway_http_request_duration_seconds` | histogram | `route`, `method` | `histogram_quantile(0.95, sum by (le) (rate(gateway_http_request_duration_seconds_bucket{route="/api/v1/sagas"}[5m])))` |
| Traffic | `gateway_http_requests_total` | counter | `route`, `method`, `status_class` | `sum by (route) (rate(gateway_http_requests_total[5m]))` |
| Errors | `gateway_http_requests_total` | counter | `status_class="5xx"` | `sum(rate(gateway_http_requests_total{route="/api/v1/sagas",status_class="5xx"}[5m])) / sum(rate(gateway_http_requests_total{route="/api/v1/sagas"}[5m]))` |
| Saturation | `gateway_rate_limited_requests_total` | counter | `route` | `sum(rate(gateway_rate_limited_requests_total[5m]))` (429 from `limit_req zone=api`) |

## opti-workflow (orchestrator)

| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | `workflow_http_request_duration_seconds` | histogram | `route`, `method` | `histogram_quantile(0.95, sum by (le) (rate(workflow_http_request_duration_seconds_bucket{route="/api/v1/sagas/place-order"}[5m])))` |
| Traffic | `workflow_sagas_total` | counter | `saga_type`, `outcome` | `sum by (saga_type) (rate(workflow_sagas_total[5m]))` |
| Errors | `workflow_sagas_total` | counter | `outcome="failed"` | `sum(rate(workflow_sagas_total{saga_type="place-order",outcome="failed"}[5m])) / sum(rate(workflow_sagas_total{saga_type="place-order"}[5m]))` |
| Errors (per step) | `workflow_saga_step_failures_total` | counter | `step`, `failure_reason` | `sum by (step, failure_reason) (increase(workflow_saga_step_failures_total[1h]))` |
| Saturation | `workflow_sagas_pending` | gauge | `status` (`RUNNING`, `COMPENSATING`) | `max(workflow_sagas_pending{status="RUNNING"})` - sagas waiting for the resume task |

## opti-customers-api

| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | `customers_http_request_duration_seconds` | histogram | `route`, `method` | `histogram_quantile(0.95, sum by (le) (rate(customers_http_request_duration_seconds_bucket{route="/api/v1/patients/{id}"}[5m])))` |
| Traffic | `customers_http_requests_total` | counter | `route`, `method`, `status_class` | `sum(rate(customers_http_requests_total{route="/api/v1/patients/{id}"}[5m]))` |
| Errors | `customers_http_requests_total` | counter | `status_class="5xx"` | `sum(rate(customers_http_requests_total{status_class="5xx"}[5m])) / sum(rate(customers_http_requests_total[5m]))` |
| Saturation | `hikaricp_connections_pending` | gauge | `pool` | `max(hikaricp_connections_pending{pool="customers"})` (pool of 10) |

## opti-products-api

| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | `products_http_request_duration_seconds` | histogram | `route`, `method` | `histogram_quantile(0.95, sum by (le) (rate(products_http_request_duration_seconds_bucket{route="/api/v1/frames/{id}/reservations"}[5m])))` |
| Traffic | `products_reservations_total` | counter | `product_type`, `outcome` | `sum by (product_type) (rate(products_reservations_total[5m]))` |
| Errors | `products_reservations_total` | counter | `outcome="insufficient_stock"` / `"error"` | `sum(rate(products_reservations_total{outcome="error"}[5m])) / sum(rate(products_reservations_total[5m]))` |
| Saturation | `hikaricp_connections_pending` | gauge | `pool` | `max(hikaricp_connections_pending{pool="products"})` |

`insufficient_stock` is a business answer, not a server error: it is graphed apart and does not
count against the availability SLO.

## opti-sales-api

| Signal | Metric | Type | Labels | PromQL |
|---|---|---|---|---|
| Latency | `sales_http_request_duration_seconds` | histogram | `route`, `method` | `histogram_quantile(0.95, sum by (le) (rate(sales_http_request_duration_seconds_bucket{route="/api/v1/work-orders"}[5m])))` |
| Traffic | `sales_work_orders_opened_total` | counter | `product_type` | `sum(rate(sales_work_orders_opened_total[5m]))` |
| Errors | `sales_http_requests_total` | counter | `route`, `status_class` | `sum(rate(sales_http_requests_total{route="/api/v1/work-orders",status_class="5xx"}[5m])) / sum(rate(sales_http_requests_total{route="/api/v1/work-orders"}[5m]))` |
| Saturation | `tomcat_threads_busy_threads` | gauge | `service` | `max(tomcat_threads_busy_threads{service="opti-sales-api"}) / 100` (`threads.max: 100`) |

## Label cardinality

| Label | Values | Estimated series | Rule |
|---|---|---|---|
| `route` | path template, not the raw URL: about 12 per API | 12 x 5 services | `/api/v1/patients/{id}`, never `/api/v1/patients/6f1c...` |
| `method` | GET, POST | 2 | - |
| `status_class` | 2xx, 4xx, 5xx | 3 | the exact code goes to the log, not to a label |
| `saga_type` | place-order, cancel-order | 2 | - |
| `outcome` | completed, compensated, failed (+ insufficient_stock, error in products) | 3-5 | closed list |
| `step` | check-patient, reserve-stock, open-order, cancel-order, release-stock | 5 | - |
| `failure_reason` | the 9 values of `FailureReason` | 9 | closed enum, safe |
| `product_type` | FRAME, LENS, ACCESSORY, LIQUID | 4 | - |

Biggest metric: `workflow_saga_step_failures_total` = 5 steps x 9 reasons = 45 series, fine.

Forbidden labels and what replaces them:

| Forbidden | Why | Replacement |
|---|---|---|
| `patient_id`, `seller_id`, `saga_id`, `work_order_number` | one series per patient/order, unbounded | the log line and the `trace_id` |
| `document_number`, `email` | personal data and unbounded | never stored anywhere in metrics |
| raw URL with ids | unbounded | `route` template |
| `sku` | thousands of frames | `product_type` |

## Latency buckets

`0.1, 0.25, 0.5, 1, 2, 3, 5, 10` seconds (same as `WorkflowMetrics.BUCKETS`).

- A seller is in front of the patient at the counter: the quotation should appear in under 1 s,
  and 2-3 s already feels slow. So there are three buckets under 1 s and a cut at 2 s and 3 s.
- `5` matches `HTTP_TIMEOUT=PT5S` of `opti-workflow`: requests stuck in the bucket 5 to 10 s are
  the ones that waited for a full timeout and a retry.
- `10` catches a saga with 3 attempts (`HTTP_ATTEMPTS`) plus back-off; above that the gateway
  `proxy_read_timeout 30s` is the last limit.
