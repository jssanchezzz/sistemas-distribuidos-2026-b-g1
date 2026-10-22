# CP6 - SLIs, SLOs and error budget

Journey: place-order (`POST /api/v1/sagas/place-order` on `opti-workflow`, through `opti-api-gateway`).
Metrics come from `04-metrics.md` and `03-instrumentation/workflow-tracing`.

## Traffic estimate

OptiView runs today for one pilot store (vision H1, `01-context/scope.md` assumption 1). The store
has about 4 sellers in `opti-sales-portal`, opens Monday to Saturday from 8:00 to 18:00, and a seller
opens around 30 quotations a day (each patient usually asks for 2 or 3 frame options, and every
option is one saga).

| Value | Calculation | Result |
|---|---|---|
| Sagas per day | 4 sellers x 30 | 120 |
| Sagas per 30-day window | 120 x 30 (Sunday traffic is close to 0, Saturday is double, so it evens out) | 3,600 |
| Average per opening hour | 120 / 10 h | 12 per hour |
| Peak (Saturday 10:00-12:00) | about twice the average | 25 per hour |
| Opening minutes per window | 26 days x 10 h x 60 | 15,600 min |

## SLO 1 - Availability

- **SLI:** sagas that did not fail for a technical reason / all place-order sagas. A business answer
  (`INSUFFICIENT_STOCK`, `PATIENT_NOT_ACTIVE`, saga `COMPENSATED`) is a good event: the system
  worked and told the seller the truth.
- **PromQL:**
  `1 - (sum(rate(workflow_sagas_total{saga_type="place-order",outcome="failed"}[30d])) / sum(rate(workflow_sagas_total{saga_type="place-order"}[30d])))`
- **Target:** 99.5 % over a rolling 30-day window.
- **Why not 99.9 % (NFR-002)?** With 3,600 sagas, 99.9 % allows 3.6 failures a month: one bad
  restart of the single Docker host would spend it all. 99.5 % is still strict for a store that
  worked on paper until August.
- **Budget:** 0.5 % x 3,600 = **18 failed sagas** per window. In time: 0.5 % x 15,600 =
  **78 minutes** of total outage during opening hours (216 minutes if we counted the whole 30 days,
  but a failure at 3 a.m. affects nobody).

## SLO 2 - Latency

- **SLI:** place-order requests answered in less than 1 s / all place-order requests.
- **PromQL:**
  `sum(rate(workflow_http_request_duration_seconds_bucket{route="/api/v1/sagas/place-order",le="1"}[30d])) / sum(rate(workflow_http_request_duration_seconds_count{route="/api/v1/sagas/place-order"}[30d]))`
- **Target:** 99 % under 1 s over 30 days.
- **Why 1 s:** the seller is at the counter with the patient. A normal saga is 3 HTTP calls of
  50-100 ms each plus Redis writes (about 300 ms). NFR-001 asks P99 < 500 ms per critical
  endpoint, and this request chains three of them, so 1 s for the whole saga is fair.
- **Budget:** 1 % x 3,600 = **36 slow requests** per window, or 1 % x 15,600 = **156 minutes** of
  "everything is slow" during opening hours.

## SLO 3 - Saga consistency (fits our product)

A saga that ends `FAILED` because a compensation failed leaves a frame reserved in
`products.stock_reservation` that nobody can sell, or a quotation open in `sales.work_order`
without stock. For a store with a small inventory this hurts more than a slow screen.

- **SLI:** sagas that reached `COMPLETED` or `COMPENSATED` without `COMPENSATION_FAILED` / all sagas.
- **PromQL:**
  `1 - (sum(increase(workflow_saga_step_failures_total{failure_reason="COMPENSATION_FAILED"}[30d])) / sum(increase(workflow_sagas_total{saga_type="place-order"}[30d])))`
- **Target:** 99.9 % over 30 days.
- **Budget:** 0.1 % x 3,600 = **3 sagas** per window that an operator must fix by hand
  (release the reservation with `POST /api/v1/reservations/{id}/release`).

## Summary

| SLO | Target | Window | Budget (requests) | Budget (opening minutes) |
|---|---|---|---|---|
| Availability | 99.5 % | 30 d | 18 | 78 |
| Latency < 1 s | 99 % | 30 d | 36 | 156 |
| Consistency | 99.9 % | 30 d | 3 | - (counted per saga) |

## Error budget policy of the team

| Budget used | What the team does |
|---|---|
| < 50 % | Normal sprint. Releases of `opti-workflow`, `opti-products-api` and `opti-sales-api` follow the usual develop -> qa -> main flow. |
| 50 % | Warning in the daily. Every new PR that touches the place-order journey needs a test of the failure path (timeout, 422) before merge. The owner of the service that spent the budget opens an HU to fix the cause. |
| 75 % | Only bug fixes and reliability work go to `main`. New features of the journey (for example new `product_type` values) wait. Each deploy is done in opening-hours-free time with a rollback plan written in the PR. |
| 100 % | Feature freeze for the journey until the 30-day window recovers (same rule as NFR-002). A blameless postmortem is mandatory, and its action items enter the next sprint as top priority. |
