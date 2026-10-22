# CP8 - Runbook: place-order fast burn

Alerts: `PlaceOrderAvailabilityFastBurn` and `PlaceOrderLatencyFastBurn` (`severity: page`, `team: opti`).
Severity per `13-operations/incident-management.md`: **P1** (sellers cannot quote; there is a
workaround: write the quotation on paper and enter it later).

## 1. Symptoms

- Sellers in `opti-sales-portal` see "Create quotation" spinning for seconds or an error
  `SERVICE_UNAVAILABLE` / `PARTICIPANT_UNAVAILABLE`.
- Sagas end `FAILED` with `failedStep` = `check-patient`, `reserve-stock` or `open-order`.
- In Grafana the place-order panel shows the error ratio over 7.2 % or more than 14.4 % of requests over 1 s.

## 2. Confirm (5 minutes)

1. Is it real? Error and slow ratio of the last hour:
   ```promql
   workflow:place_order_errors:ratio_rate1h
   workflow:place_order_slow:ratio_rate1h
   ```
2. Which step and which reason:
   ```promql
   sum by (step, failure_reason) (increase(workflow_saga_step_failures_total[30m]))
   ```
3. Which participant is slow (p95 per downstream API):
   ```promql
   histogram_quantile(0.95, sum by (le) (rate(products_http_request_duration_seconds_bucket{route="/api/v1/frames/{id}/reservations"}[5m])))
   histogram_quantile(0.95, sum by (le) (rate(customers_http_request_duration_seconds_bucket{route="/api/v1/patients/{id}"}[5m])))
   histogram_quantile(0.95, sum by (le) (rate(sales_http_request_duration_seconds_bucket{route="/api/v1/work-orders"}[5m])))
   ```
4. Take one failed request: the seller sees `traceId` in the error message (it is the
   `X-Correlation-Id`). Search all services by that id, ordered by time:
   ```
   trace_id:"4bf92f3577b34da6a3ce929d0e0e4736" | sort timestamp asc
   ```
   Look for `downstream.timeout`, `downstream.unavailable` or a `duration_ms` far above 100.
5. What changed in the last 2 hours? `git log` of the release tags (`c2-release-tags.md`) and
   `docker ps` on the host (container start times).

## 3. First mitigations (lowest risk first)

| # | Mitigation | When | How |
|---|---|---|---|
| 1 | **Rollback** the participant that got slow/failing | a deploy happened in the last 2 h | redeploy the previous image tag (for example `opti-products-api:2.0.0`) with `docker compose -f opti-infra/... up -d opti-products-api` |
| 2 | **Restart** the participant container | no deploy, the service is stuck (pool exhausted, memory) | `docker compose restart opti-products-api`; sagas left `RUNNING` are resumed by the workflow after `SAGA_RESUME_AFTER` (30 s) with the same `Idempotency-Key`, so nothing is reserved twice |
| 3 | **Tune timeouts/retries** of the workflow | a participant is slow but answers | lower `HTTP_TIMEOUT` (default `PT5S`) or `HTTP_ATTEMPTS` (3) in the `opti-workflow` env and restart it, so sellers get a fast answer instead of 15 s of retries |

Not available today, so not in the list: feature flags (we have none), scale out (one Docker host,
see `10-devops/environments.md`) and circuit breaker (`05-architecture/overview.md`: "No").

Before each action: write it in the incident channel and wait for the Incident Commander's OK.

## 4. Escalation

| Time | Who | Role (incident-management.md) |
|---|---|---|
| 0-5 min | On-call engineer of the week | acknowledges the page, confirms, starts mitigation |
| 15 min without improvement | Tech lead of the domain that is failing (customers, products or sales owner) | diagnosis of the participant |
| P1 confirmed | Incident Commander (Scrum Master of the team) | coordinates, decides rollback |
| P1 confirmed | Comms lead (Product Owner) | tells the store manager to use the paper workaround |

## 5. Verify recovery

- `workflow:place_order_errors:ratio_rate5m` back under 0.5 % and `workflow:place_order_slow:ratio_rate5m` under 1 % for 15 minutes.
- `GET /health` answers 200 on the gateway, workflow and the three APIs.
- No saga stuck: `workflow_sagas_pending{status="RUNNING"}` is 0 after 2 minutes.
- No frame left reserved by failed sagas: check `products.stock_reservation` rows with
  `status = 'RESERVED'` whose `reference` (saga id) belongs to a `FAILED` saga, and release them.
- A seller creates a test quotation and it opens in under 1 s.
- The alert resolves by itself. Then open the postmortem (`08-incident.md` template).
