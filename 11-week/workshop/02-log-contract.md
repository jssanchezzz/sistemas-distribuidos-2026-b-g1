# CP2 - Structured log contract

One JSON object per line, to stdout, in every service of the place-order journey
(`opti-api-gateway`, `opti-workflow`, `opti-customers-api`, `opti-products-api`, `opti-sales-api`).
The Java services already use `LogstashEncoder` with `correlationId` from the MDC; this contract
renames it to `correlation_id` and adds the trace fields and the domain fields.

## Mandatory fields

| Field | Type | Req. | Example | Why it exists |
|---|---|---|---|---|
| `timestamp` | string ISO-8601 UTC | yes | `2026-10-20T23:41:07.112Z` | order the lines of five services on one time line |
| `level` | enum `ERROR/WARN/INFO/DEBUG` | yes | `ERROR` | filter what needs a person |
| `service` | string | yes | `opti-workflow` | which repo/container wrote it (repo name, not `ms-*`) |
| `version` | string semver | yes | `2.0.0` | tie a failure to a release tag (c2-release-tags.md) |
| `environment` | enum `dev/qa/main` | yes | `qa` | same branches as the git workflow |
| `trace_id` | string 32 hex | yes | `4bf92f3577b34da6a3ce929d0e0e4736` | one query returns the whole request |
| `span_id` | string 16 hex | yes | `a3ce929d0e0e4736` | which hop inside the trace |
| `correlation_id` | string `[A-Za-z0-9._-]{1,64}` | yes | `7d1e0c2f9a4b` | the id the gateway already returns as `traceId` in the error envelope |
| `event` | string `domain.action` | yes | `saga.step.failed` | stable name to count and alert on |
| `message` | string | yes | `products-api did not answer` | short text for a person, no personal data |
| `duration_ms` | integer | yes (0 if not timed) | `5003` | find the slow hop without a tracing backend |

## Domain fields (optional, only when they apply)

| Field | Type | Example | Why |
|---|---|---|---|
| `saga_id` | uuid | `9a7e1c34-...` | the saga of `opti-workflow`; also `reference` in `sales.work_order` and `products.stock_reservation`, so it joins the three databases |
| `saga_step` | enum `check-patient/reserve-stock/open-order` | `reserve-stock` | which step of ADR-004 failed or was compensated |
| `failure_reason` | enum `FailureReason` | `INSUFFICIENT_STOCK` | closed list already returned to the client; safe to log and to count |
| `patient_id` | uuid | `6f1c2a9e-...` | find every order of one patient without logging who the patient is |
| `product_type` + `product_id` | enum + uuid | `FRAME`, `c0a8...` | which catalog (frames, lenses, accessories, liquids) was reserved |
| `reservation_id` | uuid | `b71d...` | the row of `products.stock_reservation` to release by hand if a compensation fails |
| `work_order_number` | string `OT-NNNN` | `OT-0142` | what the seller reads on screen and tells support |
| `downstream` + `http_status` | string + int | `opti-products-api`, `422` | who answered what, on outbound calls |

## Never log

| Data | Where it lives | How it is masked |
|---|---|---|
| `document_number`, `document_type` of the patient | `customers.patient` | not logged; use `patient_id` |
| `first_name`, `last_name`, `fullName` (returned by customers-api to the workflow as `patientName`) | `customers.patient`, saga data | not logged; the workflow keeps it only in the saga state |
| `phone`, `email`, `birth_date`, `eps`, `city` | `customers.patient` | not logged; email as `d***@***.com` if a support case needs it |
| optical formula (sphere, cylinder, axis) - health data, Ley 1581 | customers domain | never logged, not even masked |
| `Authorization` header (user JWT and the workflow `SERVICE_TOKEN`) | headers | header dropped by the logger; only `sub` (seller id) and role |
| `DATABASE_PASSWORD`, `REDIS_PASSWORD`, `JWT` private key | environment | never read by the logging code |
| payment data from Wompi (card, reference token) | `sales.payment` | only the last 4 digits and the payment id |

## Seven lines of one failed request (same `trace_id`)

The seller tries to quote frame `MNT-0231` for a patient. The first reservation call times out,
the retry reaches products-api, which refuses it because there is no stock left.

```json
{"timestamp":"2026-10-20T23:41:02.004Z","level":"INFO","service":"opti-api-gateway","version":"2.0.0","environment":"qa","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"00f067aa0ba902b7","correlation_id":"7d1e0c2f9a4b","event":"http.request.received","message":"POST /api/v1/sagas/place-order","duration_ms":0}
{"timestamp":"2026-10-20T23:41:02.031Z","level":"INFO","service":"opti-workflow","version":"2.0.0","environment":"qa","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"b7ad6b7169203331","correlation_id":"7d1e0c2f9a4b","event":"saga.started","message":"place-order saga started","duration_ms":0,"saga_id":"9a7e1c34-52f0-4d8e-b0a1-6c2f8e1d3a90","patient_id":"6f1c2a9e-3b7d-4c51-9a0e-2d8f4b6c1e77","product_type":"FRAME"}
{"timestamp":"2026-10-20T23:41:02.079Z","level":"INFO","service":"opti-workflow","version":"2.0.0","environment":"qa","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"5c3e1a9d8f7b6a21","correlation_id":"7d1e0c2f9a4b","event":"saga.step.completed","message":"patient is active","duration_ms":46,"saga_id":"9a7e1c34-52f0-4d8e-b0a1-6c2f8e1d3a90","saga_step":"check-patient","downstream":"opti-customers-api","http_status":200}
{"timestamp":"2026-10-20T23:41:07.083Z","level":"WARN","service":"opti-workflow","version":"2.0.0","environment":"qa","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"e4f2a1b9c8d70356","correlation_id":"7d1e0c2f9a4b","event":"downstream.timeout","message":"products-api did not answer in 5 s, retry 1 of 3","duration_ms":5003,"saga_id":"9a7e1c34-52f0-4d8e-b0a1-6c2f8e1d3a90","saga_step":"reserve-stock","downstream":"opti-products-api"}
{"timestamp":"2026-10-20T23:41:07.441Z","level":"WARN","service":"opti-products-api","version":"2.0.0","environment":"qa","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"9f8e7d6c5b4a3921","correlation_id":"7d1e0c2f9a4b","event":"reservation.rejected","message":"insufficient stock","duration_ms":12,"product_type":"FRAME","product_id":"c0a80164-7e2b-4f1a-9d3c-5b6a7e8f9012","sku":"MNT-0231","requested":1,"available":0}
{"timestamp":"2026-10-20T23:41:07.452Z","level":"WARN","service":"opti-workflow","version":"2.0.0","environment":"qa","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"1a2b3c4d5e6f7081","correlation_id":"7d1e0c2f9a4b","event":"saga.compensated","message":"reserve-stock refused, nothing to undo","duration_ms":5421,"saga_id":"9a7e1c34-52f0-4d8e-b0a1-6c2f8e1d3a90","saga_step":"reserve-stock","failure_reason":"INSUFFICIENT_STOCK","http_status":422}
{"timestamp":"2026-10-20T23:41:07.455Z","level":"INFO","service":"opti-api-gateway","version":"2.0.0","environment":"qa","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"00f067aa0ba902b7","correlation_id":"7d1e0c2f9a4b","event":"http.request.completed","message":"POST /api/v1/sagas/place-order -> 201 COMPENSATED","duration_ms":5451,"http_status":201}
```

Read only by `trace_id`, the story is: the patient was fine, products-api was slow once, then said
there is no stock of `MNT-0231`, the saga ended `COMPENSATED` with nothing reserved, and the seller
waited 5.4 s for that answer. The 5 s of the first attempt is the `HTTP_TIMEOUT` of the workflow.

## When to use each level

| Level | Rule for the team | Example in OptiView |
|---|---|---|
| ERROR | someone must act; the system is not consistent or a dependency is down after retries | saga ends `FAILED` because `release` of a reservation failed (`COMPENSATION_FAILED`) |
| WARN | something went wrong but the system handled it (retry, business refusal, compensation) | `INSUFFICIENT_STOCK`, `PATIENT_NOT_ACTIVE`, one timeout that a retry solved |
| INFO | a business fact or one access line per request | `saga.started`, `saga.completed` with `work_order_number` `OT-0142` |
| DEBUG | detail for a developer, off in `qa` and `main` | body size and retry back-off chosen by `ParticipantClient` |
