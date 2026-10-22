# CP9 - Incident drill and blameless postmortem

**Drill scenario (simulated):** the hop marked in CP1 step 4, `opti-workflow -> opti-products-api`
`POST /api/v1/frames/{frameId}/reservations`, answers in about 3 s for 25 % of the requests during
40 minutes, Saturday 2026-10-17 from 10:20 to 11:00 (store peak, about 25 sagas per hour).

## 1. Trace of one slow request

```
trace_id 7c2d9e41a0b35f86c1e4d2a97b30f518                          total 3,412 ms
- opti-api-gateway   POST /api/v1/sagas/place-order                  3,412 ms
  - opti-workflow    POST /api/v1/sagas/place-order                  3,398 ms
    - saga place-order (9a1f...)                                     3,381 ms
      - step check-patient -> GET customers-api                         48 ms
        - opti-customers-api GET /api/v1/patients/{id}                  41 ms
          - SELECT customers.patient                                     6 ms
      - step reserve-stock -> POST products-api                      3,105 ms   <-- slow
        - opti-products-api POST /api/v1/frames/{id}/reservations    3,096 ms
          - SELECT sum(quantity) FROM products.stock_movement        3,071 ms   <-- cause
          - INSERT products.stock_reservation                            9 ms
      - step open-order -> POST sales-api                              182 ms
        - opti-sales-api POST /api/v1/work-orders                      171 ms
          - INSERT sales.work_order / work_order_item / invoice         38 ms
      - HSET saga state in Redis (x3)                                    6 ms
```

3,105 ms is under the workflow `HTTP_TIMEOUT` of 5 s, so there is no timeout and no retry: the saga
completes, only slowly.

## 2. Four log lines of that trace (CP2 format)

```json
{"timestamp":"2026-10-17T15:41:12.020Z","level":"INFO","service":"opti-workflow","version":"2.0.0","environment":"main","trace_id":"7c2d9e41a0b35f86c1e4d2a97b30f518","span_id":"a41c09e2d7b35f60","correlation_id":"c3f19b02e7aa","event":"saga.started","message":"place-order saga started","duration_ms":0,"saga_id":"9a1f6c3e-2b7d-4e18-a5c0-71d2e9f4b836","product_type":"FRAME"}
{"timestamp":"2026-10-17T15:41:15.162Z","level":"WARN","service":"opti-products-api","version":"2.0.1","environment":"main","trace_id":"7c2d9e41a0b35f86c1e4d2a97b30f518","span_id":"e8b2f71c04d9a356","correlation_id":"c3f19b02e7aa","event":"reservation.slow","message":"stock check took longer than 1 s","duration_ms":3096,"product_type":"FRAME","product_id":"0b8e4f2a-9c13-4d7e-b6a5-3f21c8d0e947","sku":"MNT-0231"}
{"timestamp":"2026-10-17T15:41:15.175Z","level":"INFO","service":"opti-workflow","version":"2.0.0","environment":"main","trace_id":"7c2d9e41a0b35f86c1e4d2a97b30f518","span_id":"5d07c3a1e92bf846","correlation_id":"c3f19b02e7aa","event":"saga.step.completed","message":"stock reserved","duration_ms":3105,"saga_id":"9a1f6c3e-2b7d-4e18-a5c0-71d2e9f4b836","saga_step":"reserve-stock","downstream":"opti-products-api","http_status":201}
{"timestamp":"2026-10-17T15:41:15.418Z","level":"INFO","service":"opti-workflow","version":"2.0.0","environment":"main","trace_id":"7c2d9e41a0b35f86c1e4d2a97b30f518","span_id":"a41c09e2d7b35f60","correlation_id":"c3f19b02e7aa","event":"saga.completed","message":"place-order completed","duration_ms":3398,"saga_id":"9a1f6c3e-2b7d-4e18-a5c0-71d2e9f4b836","work_order_number":"OT-0188","http_status":201}
```

(Timestamps are UTC: 10:41 in Colombia is 15:41Z.)

## 3. SLIs during the incident

| SLI (05-slo.md) | Normal | During the 40 minutes | Why |
|---|---|---|---|
| Availability | 100 % | **100 %** | 3 s < `HTTP_TIMEOUT` 5 s, every saga completed |
| Latency < 1 s | 99.6 % | **75 %** | 1 of every 4 sagas took about 3.4 s |
| Consistency | 100 % | **100 %** | no compensation was needed |

## 4. Which alert fires first, and when

The slow ratio in the 5-minute window jumps to 0.25 in the first minutes, above 0.144, but the
1-hour window grows slowly: 0.25 x t / 60 > 0.144 when t > 34.6 minutes. With `for: 2m`,
**`PlaceOrderLatencyFastBurn` fires at minute 37 (10:57)**. No availability alert fires (no
failures). The 6 h slow-burn alerts never fire, because the incident stops at minute 40.

Runbook step that stops the bleeding: **section 2 step 3** shows `opti-products-api` p95 at 3 s,
the trace shows the `stock_movement` query, and step 5 shows `opti-products-api:2.0.1` was deployed
Friday 18:30. **Section 3 mitigation 1 (rollback to 2.0.0)** is done at minute 40 (11:00).

## 5. Blameless postmortem - INC-001

**Summary:** for 40 minutes on Saturday morning one in four quotations took over 3 s because the
stock check of `opti-products-api` 2.0.1 was slow for some frames. No sale was lost and no data was
left inconsistent.

**Timeline (2026-10-17, Colombia time)**

| Time | Minute | Event |
|---|---|---|
| Fri 18:30 | - | `opti-products-api` 2.0.1 deployed; it recalculates available stock with `sum(quantity)` over `products.stock_movement` |
| 10:20 | 0 | Saturday peak starts; frames with a long movement history (the best sellers, ~25 % of requests) take ~3 s |
| 10:32 | 12 | A seller says "the system is slow" to the store manager; nobody tells the team |
| 10:57 | 37 | `PlaceOrderLatencyFastBurn` pages the on-call |
| 10:58 | 38 | On-call acks, opens the runbook, finds the slow span in the trace |
| 10:59 | 39 | IC approves rollback of `opti-products-api` to 2.0.0 |
| 11:00 | 40 | Rollback done; slow ratio (5 m) back to 0 |
| 11:15 | 55 | Recovery verified (runbook section 5); alert resolved |

**Impact:** about 17 sagas in 40 minutes, 4 of them over 1 s (about 3.4 s each). Latency budget:
4 of 36 slow requests = **11 %** spent; in minutes, 40 x 25 % = 10 equivalent minutes of 156 =
**6.4 %**. Availability and consistency budgets: 0 % spent.

**Root cause:** the new stock check sums all `products.stock_movement` rows of a frame and the table
has no index on `(frame_id, created_at)`. Frames with hundreds of movements took 3 s.

**Contributing factors**
- The QA database has few movements per frame, so the query was fast there.
- Detection took 37 minutes: the 1-hour window of the fast-burn alert is slow for a partial slowdown.
- Deploys on Friday evening right before the busiest day.
- The seller's complaint at minute 12 had no channel to reach the team.

**Action items**

1. **HU-OBS-006 - Index stock movements.** As the products owner, I want an index on
   `products.stock_movement(frame_id, created_at)` in `opti-products-db`, so that the stock check stays fast for best-selling frames.
   - Given a frame with 5,000 movements in QA, when `POST /api/v1/frames/{id}/reservations` is called, then p95 < 150 ms.
   - Given the migration, when it runs on `main`, then it uses `CREATE INDEX CONCURRENTLY` and no table lock appears.
2. **HU-OBS-007 - Faster latency detection.** As the on-call, I want an extra latency alert with 5 m and 30 m windows and burn rate 14.4, so that a partial slowdown pages in under 10 minutes.
   - Given 25 % of place-order requests over 1 s, when the rule is evaluated with `promtool test rules`, then it fires before minute 10.
   - Given the same rule with 0 slow requests, then it never fires.
3. **HU-OBS-008 - Load test with real data volume.** As the team, I want a k6 test of place-order with a QA database seeded with production-like `stock_movement` volume, run before any release of the journey, so that slow queries appear before Saturday.
   - Given a release tag of `opti-products-api`, when the pipeline runs, then the k6 test of 25 sagas/min fails the build if p95 > 1 s.
   - Given a release on Friday after 15:00, then the checklist blocks the deploy to `main` until Monday.
