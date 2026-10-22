# CP7 - Alert policy

Rules in [06-alerts.yml](06-alerts.yml), checked with `promtool check rules 06-alerts.yml`
(`SUCCESS: 9 rules found`). They use the CP4 metrics `workflow_sagas_total` and
`workflow_http_request_duration_seconds` and the SLOs of `05-slo.md`.

## How much budget each alert has spent before it fires

Burn rate = how many times faster than "exactly on budget" we are spending. Spending at rate 1
uses 100 % of the budget in 30 days (720 h).

| Alert | Burn rate | Windows | Budget spent when it fires | In our numbers | Action |
|---|---|---|---|---|---|
| `PlaceOrderAvailabilityFastBurn` | 14.4 | 1 h, confirmed 5 m | 14.4 x 1 h / 720 h = **2 %** | error ratio > 7.2 %; 2 % of 18 = 0.36 sagas, so at 12 sagas/h it means about 1 technical failure in the hour plus another in the last 5 minutes | page the on-call (`severity: page`) |
| `PlaceOrderAvailabilitySlowBurn` | 6 | 6 h, confirmed 30 m | 6 x 6 h / 720 h = **5 %** | error ratio > 3 %; 5 % of 18 = 0.9, about 2 failures in 72 sagas of a 6 h day | ticket for the next working day |
| `PlaceOrderLatencyFastBurn` | 14.4 | 1 h, confirmed 5 m | **2 %** of the latency budget | more than 14.4 % of requests over 1 s; at the Saturday peak of 25/h that is 4 slow quotations in an hour | page the on-call |

If the fast alert were ignored, the whole availability budget (18 sagas, 78 minutes) would be gone
in 720 / 14.4 = **50 hours**, which is 5 opening days. With the slow alert, it would last
720 / 6 = 120 hours, 12 opening days.

### Limitation: low traffic

12 sagas per hour is little. One failed saga in a quiet hour already crosses 7.2 %. We accept it for
now because each failure is a seller who could not quote a frame in front of a patient. If the
pages are too noisy, the next step is to add `and sum(increase(workflow_sagas_total[1h])) >= 10`
to the fast rule, so it only fires with enough traffic.

## Cause-based alerts we will NOT create

| Alert | Why not |
|---|---|
| "CPU of `opti-products-api` container > 80 % for 5 minutes" | High CPU does not mean a seller is waiting. During the nightly `opti-worker` jobs CPU goes up and nobody is using the portal. If CPU hurts users, `PlaceOrderLatencyFastBurn` fires anyway. CPU stays on the dashboard, not in the pager. |
| "Hikari pool of `opti-sales-api` has pending connections" | A few pending connections for some seconds are normal (pool of 10, wait 5 s). It is a cause we look at in the runbook while diagnosing, not a symptom. Paging on it would wake someone up with no user impact. |
| (bonus) "Any 4xx from `opti-products-api`" | `422 insufficient stock` is the correct business answer. It is counted as a good event in the SLO and must never page. |
