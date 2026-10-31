<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       13-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 13

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Sebastian Sanchez Silva
- GITHUB_USER: jssanchezzz
- TEAM: The illusionists
- SPRINT_GOAL: Threat model of TB4 (opti-sales-api to Wompi) and turn its top risks into security backlog stories
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| SEC-061 | Only ADMIN or SELLER can trigger a Wompi charge (E, risk 6, P0) | todo | https://github.com/code-corhuila/opti-docs/issues/61 |
| SEC-062 | Do not send or return the customer's Nequi phone in Wompi references and errors (I, risk 6, P1) | todo | https://github.com/code-corhuila/opti-docs/issues/62 |
| SEC-063 | Add timeout and circuit breaker to the Wompi calls (D, risk 4, P2) | todo | https://github.com/code-corhuila/opti-docs/issues/63 |
| W13-STRIDE | STRIDE of TB4 opti-sales-api to Wompi | done (PR open for review) | https://github.com/code-corhuila/opti-docs/pull/60 |

## 2. My individual contribution
- Claimed boundary TB4 in https://github.com/code-corhuila/opti-docs/issues/59
- Attack surface map with TB1-TB5, asset inventory and TB4 entry points: `13-week/workshop/01-attack-surface.md`
- STRIDE table, risk ranking, residual risk and library references: `05-architecture/security/stride-jssanchezzz.md` in opti-docs (PR #60)
- Three security stories in the opti-docs backlog (#61, #62, #63)
- Security gates proposal (gitleaks, OWASP dependency-check, authorization tests) and negative test `PaymentsAuthzTest`: `13-week/workshop/04-security-ci/`

## 3. Blockers and risks
- The negative test needs JDK 21 and the Maven jars; my laptop only has Java 8, so the real console output is still pending.
- `PaymentsAuthzTest` lives in my fork; it has to be copied into `opti-sales-api` before `-Dtest='*AuthzTest'` finds it in CI.
- Commit `1e01efd` added 156 lines, over the 150-line limit of the workshop.

## 4. Plan for next week
- Run the negative test with JDK 21 and paste the output in `HOW-TO-RUN.md`.
- Answer the peer review on PR #60.
- Propose the P0 story (#61) for the next sprint of opti-sales-api.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Workshop folder: https://github.com/jssanchezzz/sistemas-distribuidos-2026-b-g1/tree/main/13-week/workshop
- Claim issue: https://github.com/code-corhuila/opti-docs/issues/59
- STRIDE PR: https://github.com/code-corhuila/opti-docs/pull/60
- Stories: https://github.com/code-corhuila/opti-docs/issues/61, https://github.com/code-corhuila/opti-docs/issues/62, https://github.com/code-corhuila/opti-docs/issues/63
