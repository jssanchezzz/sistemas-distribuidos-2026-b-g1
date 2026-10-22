<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       11-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 11

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Sebastian Sanchez Silva
- GITHUB_USER: jssanchezzz
- TEAM: opti (The Illusionists)
- SPRINT_GOAL: Make the place-order saga observable end to end (trace context, JSON logs, metrics)
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-OBS-001 | Trace map of the place-order journey (CP1) | done |  |
| HU-OBS-002 | Structured JSON log contract for the five services of the journey (CP2) | done |  |
| HU-OBS-003 | traceparent filter, JSON logger and metrics in opti-workflow with a propagation test (CP3) | done |  |
| HU-OBS-004 | Golden-signal metric catalog with PromQL and cardinality rules (CP4) | done |  |
| HU-OBS-005 | SLOs, burn-rate alerts, runbook and incident drill (CP6-CP9) | todo |  |

## 2. My individual contribution
- Mapped the place-order saga (opti-api-gateway -> opti-workflow -> customers/products/sales APIs) with its async boundary (saga state in Redis + resume task).
- Wrote the log contract: 11 mandatory fields, saga/patient/reservation domain fields, never-log list for patient data and tokens.
- Built `workflow-tracing` (Java 21): W3C traceparent in and out, JSON logs with trace_id/span_id, `workflow_sagas_total` and `workflow_http_request_duration_seconds`, 2 passing tests.
- Wrote the metric catalog for the five services, with bounded labels and latency buckets tied to `HTTP_TIMEOUT=5s`.

## 3. Blockers and risks
- `management.endpoints.enabled-by-default: false` in the APIs: Prometheus metrics need an actuator change agreed with the team.
- The broker for `OrdenCreada` is not chosen, so the async event hop cannot be traced yet.
- The nginx gateway does not create spans; it only forwards `traceparent`.

## 4. Plan for next week
- CP6-CP10: SLOs and error budget for place-order, burn-rate alerts, runbook, incident drill and postmortem.
- Propose a PR to opti-workflow to add `traceparent` in `CorrelationFilter` and `ParticipantClient`.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- 11-week/workshop/README.md
- 11-week/workshop/01-journey.md
- 11-week/workshop/02-log-contract.md
- 11-week/workshop/03-instrumentation/
- 11-week/workshop/04-metrics.md
