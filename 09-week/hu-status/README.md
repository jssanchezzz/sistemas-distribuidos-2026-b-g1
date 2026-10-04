<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       09-week/hu-status/README.md  (inside YOUR fork). English. -->
 
# Weekly Status - Week 09
 
<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Sebastian Sanchez Silva
- GITHUB_USER: jssanchezzz
- TEAM: The illusionists
- SPRINT_GOAL: Align the `ms-ordenes` and `ms-pacientes` OpenAPI contracts with the agreed synchronous stock flow and patient-ownership rules, so implementers find no contradiction with the gateway, inventory and billing contracts.
<!-- CONFIG-END -->
 
## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-XXX-011 | Align ms-ordenes and ms-pacientes API contracts with the synchronous stock flow | done | [`docs(ordenes): align ms-ordenes and ms-pacientes contracts with synchronous stock flow`](<PR_2_COMMITS_URL>) — PR [`docs/align-api-contracts-patients-orders` → `main`](<PR_2_URL>) |
 
## 2. My individual contribution
 
During Week 09, I contributed to the OptiView project by applying the agreed API decisions to the two remaining service contracts, `07-api/contracts/openapi/ms-ordenes.yaml` and `ms-pacientes.yaml`.
 
### Main activities completed
 
- **Approval flow (`ms-ordenes`)**: Made `POST /work-orders/{id}/approve` the only way to reach `APPROVED` and removed `APPROVED` from `PATCH /work-orders/{id}/status`, so an order can no longer skip the stock decrement and the `OrdenCreada` event that triggers invoicing.
- **Synchronous stock and compensation**: Documented that approval decrements the frame and then the lens stock in `ms-inventario`, and calls `return-stock` if the second decrement fails, so no stock stays held by an order that remained in `QUOTATION`. Extended the `INSUFFICIENT_STOCK` error to cover both items.
- **Lens validation**: Order creation now also checks that the lens exists (`LENS_NOT_FOUND`, `404`), checked right after the frame, following the DEC-04 validation-order rule.
- **Role scoping (`ms-ordenes`)**: Split the status permissions into `orders:advance-status` (laboratory stages) and `orders:finalize` (`DELIVERED` / `CANCELLED`), and documented that a `PACIENTE` only sees their own orders.
- **Events**: Documented `OrdenCreada` (on approval, consumed by `ms-facturacion`) and `OrdenEstadoCambiado` (consumed by `ms-pacientes`), and fixed a stale reference to an old `domain-events.md` path.
- **Formulas list (`ms-pacientes`)**: `GET /patients/{id}/formulas` is now paginated with `page`, `limit` and `meta` like every other list endpoint, instead of returning a bare array.
- **Complete responses (`ms-pacientes`)**: Added `birthDate`, `address` and `gender` to `PatientResponse`, and `optometristName` and `formulaDate` to `OpticalFormulaResponse`, so responses match their create requests.
- **Ownership (`ms-pacientes`)**: Added `403` responses and the "own record only" rule for `PACIENTE` on patient and formula reads, and made `overdue-controls` staff-only. Documented that the last-visit date is refreshed by consuming `OrdenEstadoCambiado`.
- **Base URLs**: Aligned both `servers.url` entries with the gateway routing table (`/api/v1/orders` and `/api/v1`).
- **Validation**: Both files parse as YAML and all their `$ref` resolve.
### Pull request
 
`docs(ordenes): align ms-ordenes and ms-pacientes contracts with synchronous stock flow` — branch `docs/align-api-contracts-patients-orders` → `main` (1 commit, 2 files).
 
## 3. Blockers and risks
 
- This PR depends on the companion PR by `DaniKaizenNetwork`, which defines `return-stock`, the permission matrix and the gateway routing that these two contracts reference; it should be merged second.
- Compensation after a partial failure (frame decremented, lens not) is documented but not yet specified for the case where `return-stock` itself fails; that needs a team decision (retry or a manual reconciliation task).
- `GET /patients` for a `PACIENTE` returns only their own record by design; the portal must not rely on searching other patients.
- The contracts were validated only for YAML syntax and `$ref` resolution, not with a linter such as Spectral.
## 4. Plan for next week
 
- Rebase this branch if the companion PR changes before merge and re-check the cross-references.
- Define the behavior when `return-stock` fails (retry policy or reconciliation) and add it to `ms-ordenes.yaml`.
- Run Spectral over the two contracts and fix what it reports.
- Add contract tests for the approval flow (stock decrement, compensation and `422` paths) once the services exist.
## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — documentation repository: the `docs/...` branch goes straight to `main` as `branching-policy.md` defines for `-docs` repositories.
- [x] Testable acceptance criteria — both files parse as YAML and every `$ref` resolves.
- [ ] Tests added/updated (unit / integration) — documentation-only change (OpenAPI contracts), no code touched.
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables
## 6. Evidence links
 
- Commit: [`docs(ordenes): align ms-ordenes and ms-pacientes contracts with synchronous stock flow`](<PR_2_COMMITS_URL>)
- PR: [`docs/align-api-contracts-patients-orders` → `main`](<PR_2_URL>)
- Contracts updated: `07-api/contracts/openapi/ms-ordenes.yaml`, `07-api/contracts/openapi/ms-pacientes.yaml`
- Shared rules referenced: `07-api/guidelines.md`, `07-api/authentication.md`
