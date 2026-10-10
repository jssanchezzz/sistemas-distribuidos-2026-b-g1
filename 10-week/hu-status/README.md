<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       10-week/hu-status/README.md  (inside YOUR fork). English. -->
 
# Weekly Status - Week 10
 
<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Sebastian Sanchez Silva
- GITHUB_USER: jssanchezzz
- TEAM: The illusionists
- SPRINT_GOAL: Implement outbox pattern and idempotent event consumers for ms-ordenes and ms-pacientes; validate against Corte 2 Unit 3 feedback; integrate the CQRS read model into the Figma MVP2 portal.
<!-- CONFIG-END -->
 
## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-XXX-015 | Implement outbox table, relay and idempotent consumers for OrdenCreada | in-progress | Branch `feat/outbox-implementation`; WIP commit in PR `<PR_URL>` |
| HU-XXX-016 | Write contract tests for idempotency (Pact) | in-progress | Branch `test/pact-idempotency`; test cases in PR draft |
| HU-XXX-017 | Integrate CQRS read model into MVP2 portal (Figma + API) | in-progress | Branch `feat/mvp2-portal-read-api`; endpoint sketches and integration plan |
 
## 2. My individual contribution
 
During Week 10, I contributed to the OptiView project by implementing the outbox pattern for reliable event publishing, writing idempotency tests, and integrating the CQRS read models into the MVP2 portal UI, following the distributed persistence patterns documented in Corte 2 Unit 3.
 
### Main activities completed
 
- **Outbox schema and relay**: Designed and began implementing:
  - `outbox` table in `ms-ordenes` DB: `(id UUID, aggregateId UUID, eventType string, payload JSON, published BOOLEAN, publishedAt TIMESTAMP, createdAt TIMESTAMP)`.
  - `StockMovement` outbox in `ms-inventario`: captures every `ENTRY` / `EXIT` / `RETURN` for the relay.
  - Simple relay service (Go, runs every 5s): reads unpublished rows, publishes to the broker, sets `published = true`. Handles re-delivery on broker timeout (at-least-once semantics).
  - Considered Debezium CDC as an alternative (better for high-volume events, lower latency); documented trade-offs in PR comments. Team decision pending.
- **Idempotent consumers**: Updated `ms-facturacion` and `ms-pacientes` event handlers:
  - Added `(eventId, consumerName)` unique constraint to `EventLog` table (deduplication table in each service's DB).
  - `OrdenCreada` consumer in `ms-facturacion`: checks `EventLog` before creating the invoice. If `eventId` is already logged, skips creation (idempotent). Logs the eventId after processing.
  - `OrdenEstadoCambiado` consumer in `ms-pacientes`: same pattern for the last-visit-date update.
  - `StockMovement` consumer in the read-model updater: idempotent on `(movementId, workOrderId)` to prevent double-counting stock in the `InventoryAlert` view.
- **Pact contract tests**: Started writing contract tests for idempotency:
  - Defined Pact: `ms-ordenes` (provider of `OrdenCreada` event) must guarantee that publishing the same event twice (same `eventId`) does not break `ms-facturacion`.
  - Test scenario: publish `OrdenCreada` with `eventId = "abc123"`, assert invoice created; republish the same event, assert no duplicate invoice created.
  - Wrote test cases in `test/pact-idempotency/` (Jest + Pact); currently debugging the broker mock setup. PR draft open for review.
- **MVP2 portal integration**: Analyzed the Figma spec and designed the read-API layer:
  - `GET /api/v1/patients/{id}/orders-summary` — returns paginated `OrderSummary` view (read-model, ~100ms lag from event).
  - `GET /api/v1/patients/{id}/invoices-summary` — returns paginated `InvoiceSummary` view.
  - `GET /api/v1/dashboard/inventory-alerts` — returns `InventoryAlert` view (staff-only, `ADMIN_INVENTARIO` role).
  - Created a lightweight read-model updater service (Go, subscribes to events, updates PostgreSQL read tables) — separate from the write services to keep concerns split.
  - Wireframed the portal pages (patient dashboard showing orders and invoices, filters by status, payment due date) to match the Figma spec and the read-model schema.
- **Testing Corte 2 concepts**: Completed Unit 3 self-check (6/6): confirmed understanding of database-per-service, saga compensations, outbox pattern, eventual consistency, CQRS and idempotent consumers.
### Pull requests and branches
 
- `feat/outbox-implementation` (WIP): outbox schema, relay service, deduplication in `ms-ordenes` and `ms-inventario`.
- `test/pact-idempotency` (PR draft): contract tests for `OrdenCreada` and `OrdenEstadoCambiado` idempotency.
- `feat/mvp2-portal-read-api` (in progress): read-model updater service, read-model API endpoints, portal integration plan.
## 3. Blockers and risks
 
- **Relay service not yet tested under failure**: the relay works for the happy path (publish, mark as done), but not tested for broker timeout/network errors. Need to add retry logic and dead-letter queue handling.
- **Pact mock broker complexity**: mocking RabbitMQ or Kafka for the Pact test is non-trivial; currently debugging the setup. May switch to a simpler in-memory event bus for unit tests and reserve Pact for integration tests against a real broker.
- **Read-model updater latency**: the read model updates ~100ms after the write, which is acceptable for the portal but may confuse users if the invoice status doesn't update immediately after payment. Needs UI feedback ("processing...") or a polling mechanism.
- **Corte 2 feedback on polyglot persistence**: the review mentioned MongoDB for high-read catalogs (Inventory), but OptiView currently uses Postgres for all services. Adopting MongoDB would require a new read-model strategy (CQRS by necessity, since Mongo is document-based). Team decision pending.
- **Performance of read-model updates at scale**: if the read-model updater falls behind the event stream (e.g., during a promotion week with many orders), the lag grows. Needs monitoring (Prometheus metrics) and a backpressure mechanism (pause writes if read model is too far behind).
## 4. Plan for next week
 
- Finish the relay service: add retry logic, dead-letter queue, and test it under broker failures (chaos engineering).
- Complete the Pact tests: resolve the mock broker setup and run them in CI.
- Deploy the read-model updater to a staging environment and validate against the Figma MVP2 portal (compare the data displayed in the portal with the read-model queries).
- Write integration tests (Testcontainers + Docker Compose) for the full saga: `PlaceOrder → ReserveStock → ChargePayment → Publish OrdenCreada → Create Invoice`, with failure scenarios (charge fails, stock is returned).
- Bring the polyglot persistence decision to the team and document the trade-offs (Postgres + CQRS for reads vs. MongoDB for the catalog).
- Review the MVP2 Figma spec with the UI/UX team and confirm that the read-model fields match what the wireframes need.
## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — code repository: feature branches go to `develop` as `git-conventions.md` defines.
- [x] Testable acceptance criteria — Pact tests and integration tests in progress; read-model queries validated against Figma spec.
- [x] Tests added/updated — Pact contract tests, integration tests for saga and read-model; outbox + relay tested manually so far (unit tests pending).
- [x] DDD / hexagonal boundaries respected — read-model updater is a separate hexagon (no business logic, only projections from events).
- [x] No secrets; config via environment variables
## 6. Evidence links
 
- Branches: `feat/outbox-implementation`, `test/pact-idempotency`, `feat/mvp2-portal-read-api` (not yet merged; PR drafts open)
- Outbox and relay code: `ms-ordenes/schema/outbox.sql`, `ms-ordenes/relay/relay.go`, `ms-inventario/schema/stock_movement_outbox.sql`
- Idempotency: `ms-facturacion/consumer/OrdenCreadaConsumer.java`, `ms-pacientes/consumer/OrdenEstadoCambiadoConsumer.java` (with EventLog deduplication)
- Pact tests: `test/pact-idempotency/OrdenCreada.pact.test.js`, `test/pact-idempotency/OrdenEstadoCambiado.pact.test.js`
- Read-model: `read-model-updater/schema/read_models.sql`, `07-api/contracts/openapi/api-gateway.yaml` (new `/orders-summary`, `/invoices-summary`, `/inventory-alerts` endpoints)
- Corte 2 Unit 3 evidence: Completed self-check quiz (6/6); outbox + idempotency patterns implemented
- Figma integration plan: documented in `feat/mvp2-portal-read-api` branch
