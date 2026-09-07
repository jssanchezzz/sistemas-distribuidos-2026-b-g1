# Weekly Status - Week 05

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->

* FULL_NAME: Juan Sebastian Sanchez Silva
* GITHUB_USER: jssanchezzz
* TEAM: The illusionists
* SPRINT_GOAL: Design and document the relational PostgreSQL database schemas, SQL triggers, partial indexes, and ER diagrams for all microservices (patients, inventory, orders, billing).

<!-- CONFIG-END -->

## 1. User stories worked this week

| HU ID      | Title                                                                | Status (todo/doing/done) | Evidence (PR or commit URL)                                                          |
| ---------- | -------------------------------------------------------------------- | ------------------------ | ------------------------------------------------------------------------------------ |
| HU-XXX-002 | Microservices Relational Database Schema Design & DDL Specification | done                     | `docs(database): define PostgreSQL DDL, triggers, and ER diagrams for microservices` |

## 2. My individual contribution

During Week 05, I designed and documented the complete PostgreSQL data model and DDL specifications across the four primary bounded contexts (`patients_schema`, `inventory_schema`, `orders_schema`, and `billing_schema`).

### Main activities completed

* **Patients Context (`patients_schema`)**:
  * Modeled tables for `optical_formulas` and `periodic_controls`.
  * Implemented DB-level invariant enforcement (`INV-PAT-003`) using a PostgreSQL trigger (`trg_set_formula_as_current`) and partial unique index (`idx_formulas_current`) to guarantee a single active prescription per patient.
* **Inventory Context (`inventory_schema`)**:
  * Modeled `suppliers`, `frames`, `lenses`, and an append-only `stock_movements` ledger.
  * Added CHECK constraints for domain invariants (`sell_price >= buy_price`, `stock_quantity >= 0`).
  * Structured forward-compatible support (`source_report_id`) for bulk supplier purchase uploads.
  * Created partial indexes (`idx_frames_stock_low`, `idx_lenses_stock_low`) for efficient low-stock queries.
* **Orders Context (`orders_schema`)**:
  * Modeled `laboratories`, `work_orders`, `order_treatments`, and `order_status_history`.
  * Configured trigger-based order code generation (`generate_order_code`) with sequences (`OT-XXXX`).
  * Applied ACL pattern via `prescription_snapshot` (JSONB) to decouple order snapshots from live patient records without cross-schema foreign keys.
* **Billing Context (`billing_schema`)**:
  * Modeled `insurance_agreements`, `invoices`, `invoice_items`, `payments`, and `daily_closings`.
  * Implemented invoice sequence triggers (`FV-XXXX`) and provided forward-compatibility for live payment gateway IDs (`provider_reference`).
  * Defined Colombia timezone (`America/Bogota`) boundary rules for accurate `daily_closings` batching.
* **Architecture & Diagrams**:
  * Designed visual Mermaid ER diagrams for each bounded context.
  * Established non-breaking forward-only database migration rules and documented PostgreSQL 16 selection rationale.

### Commit

The contribution was implemented in the following commit:

`docs(data): define per-service PostgreSQL schemas for OptiView`

## 3. Blockers and risks

* Cross-schema references (e.g., `patient_id` in `work_orders` or `invoices`) do not use database-level foreign keys to respect microservice boundaries; validation must be strictly handled at the application tier.
* Timezone handling for `daily_closings` must explicitly use `America/Bogota` (UTC-5) within aggregation jobs to prevent midnight transaction misclassification.

## 4. Plan for next week

* Review schema design and trigger logic with the backend development team.
* Initialize database migration scripts (e.g., Flyway/Liquibase or SQL migrations) matching the documented schemas.
* Set up data access repositories and entity mappings within each microservice project.

## 5. Compliance self-check

* [x] Conventional Commits - `type(scope): summary`
* [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
* [x] Testable acceptance criteria
* [ ] Tests added/updated (unit / integration)

## 6. Evidence links

* Commit: `docs(data): define per-service PostgreSQL schemas for OptiView`
* Schema DDL Documentation: `06-dara/models.md`
