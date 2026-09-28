<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       08-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 08

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Sebastian Sanchez Silva
- GITHUB_USER: jssanchezzz
- TEAM: The illusionists
- SPRINT_GOAL: Close every open behavior decision left pending in the `07-api` contract guidelines, starting with the `ms-facturacion` OpenAPI contract, so implementers have zero ambiguity on status codes, business-rule ordering and role scoping.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-XXX-009 | Close open API behavior decisions in `07-api` guidelines (ms-facturacion) | done | [`docs(api): close open behavior decisions in 07-api guidelines`](https://github.com/code-corhuila/opti-docs/pull/16/commits) — PR [`docs(api): behavior decisions in 07`](https://github.com/code-corhuila/opti-docs/pull/16) |

## 2. My individual contribution

During Week 08, I contributed to the OptiView project by closing the open behavior decisions that were still pending in the `07-api` contract guidelines, applying them concretely to the `ms-facturacion` OpenAPI contract (`07-api/contracts/openapi/ms-facturacion.yaml`).

### Main activities completed

- **DEC-01 (empty collections)**: Confirmed and documented in `GET /invoices` that an empty result set is always `200` with `data: []`, never `404`/`204`, referencing the shared decision in `../../guidelines.md`.
- **Derived balance rule (INV-BIL-002/INV-BIL-003)**: Made explicit that `outstandingBalance` on `GET /invoices/{id}` is always computed as `total - sum(payments.amount)` and is never stored independently, closing the ambiguity on how the patient-facing balance (HU-11, FR-019) should be calculated.
- **One-invoice-per-work-order rule**: Documented on `GET /invoices/by-work-order/{workOrderId}` that there is a unique constraint on `workOrderId` (`06-data/models.md`), and distinguished the specific `404` case where `OrdenCreada` has not yet been consumed from a generic not-found, with a dedicated example payload.
- **Authorization/role-scoping decision**: Closed the open question on `POST /invoices/{id}/payments` by specifying that `VENDEDOR`/`ADMIN_OPTICA` can register a payment on any invoice, while `PACIENTE` (via `portal-paciente`, HU-11) is scoped to their **own** invoice only — the use case must reject with `403` if the token's `patientId` does not match the invoice's `patientId`, even though the `PACIENTE` role itself grants `payments:create`.
- **Overpayment and status-recomputation rules (INV-BIL-002/INV-BIL-003)**: Specified that a payment is rejected with `422` if `amount` would exceed the invoice's `outstandingBalance`, and that `status` is recomputed after every payment (`PAID` when the balance reaches 0, `PARTIAL` otherwise), covering HU-10 Scenarios 2 and 3.
- **Business-rule ordering (DEC-04)**: Explicitly noted, where it applied, that a given endpoint has only one business rule to apply and therefore no `DEC-04` ordering decision is needed — removing a source of ambiguity rather than leaving it silently unaddressed.
- Added request/response examples (`amount`, `method`, error codes `PAYMENT_EXCEEDS_BALANCE` / `NOT_FOUND`) so the contract is directly testable against the acceptance criteria of HU-10.

## 3. Blockers and risks

- These behavior decisions were closed only for `ms-facturacion`; the same open decisions (DEC-01 to DEC-04) likely still need to be verified against the other service contracts under `07-api/contracts/openapi/` for consistency.
- The payment-method enum (`efectivo`, `tarjeta`, `transferencia`, `PSE`, `Nequi`, `Daviplata`, `cheque`, `otro`) is hardcoded in the contract; if the domain model (`02-domain/domain-map.md`) changes this list, the contract will need a follow-up update.

## 4. Plan for next week

- Apply the same closed-decision pass (DEC-01 to DEC-04) to the remaining `07-api` OpenAPI contracts (`ms-pacientes`, `ms-inventario`, `ms-ordenes`, `api-gateway`).
- Cross-check the authorization rule documented here against `00-governance/security-policy.md` to confirm the RBAC matrix is consistent.
- Validate the contract with a linter (e.g. Spectral) to confirm no schema errors were introduced while closing the decisions.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [X] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration) — documentation-only change (OpenAPI contract), no code touched.
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links

- Commit: [`docs(api): close open behavior decisions in 07-api guidelines`](https://github.com/code-corhuila/opti-docs/pull/16/commits)
- PR: [`docs(api): behavior decisions in 07`](https://github.com/code-corhuila/opti-docs/pull/16)
- Contract updated: `07-api/contracts/openapi/ms-facturacion.yaml`
- Shared guidelines referenced: `07-api/guidelines.md`
