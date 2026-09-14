<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       06-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 06

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
* FULL_NAME: Juan Sebastian Sanchez Silva
* GITHUB_USER: jssanchezzz
* TEAM: The illusionists
* SPRINT_GOAL: Present the OptiView MVP 1 (Corte 1) walking-skeleton demo to the class — HU-01, HU-05 and HU-08 chained end to end on the real backend (Java 21 / Spring Boot / PostgreSQL) and frontend (React) — in the team's assigned slot (Team 1, Group G1, order #5, board #15).
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-01 | Register a patient | done | `06-week/hu-status/OptiView_MVP1_Expo.pptx` (slides 4, 6) |
| HU-05 | Register a frame in inventory | done | `06-week/hu-status/OptiView_MVP1_Expo.pptx` (slides 4, 6) |
| HU-08-MVP | Create a work order (happy path only) | done | `06-week/hu-status/OptiView_MVP1_Expo.pptx` (slides 4, 6) |

## 2. My individual contribution
- Helped prepare the MVP 1 (Corte 1) presentation deck (`OptiView_MVP1_Expo.pptx`), covering the problem statement, product vision, MVP scope (HU-01, HU-05, HU-08), backend architecture (Clean Architecture, Java 21 / Spring Boot 3.5 / PostgreSQL 16 / Flyway), the chained demo flow (register patient → register frame → create work order → stock is decremented in the same transaction), JWT authentication and role-based authorization, and the roadmap toward the fixed production date (2026-11-15).
- Presented alongside the team in the class session, in the assigned order (Team 1, Group G1, presentation #5 of 18, board #15), per `orden_presentacion_equipos.jpeg`.
- Attended the 3-minute feedback round for other teams' presentations, as required by the session format.

## 3. Blockers and risks
-

## 4. Plan for next week
- Incorporate any feedback received during the MVP 1 presentation and retrospective into the backlog before starting the next sprint's HUs.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links
- `06-week/hu-status/OptiView_MVP1_Expo.pptx` — MVP 1 presentation deck
- `06-week/hu-status/orden_presentacion_equipos.jpeg` — class presentation order (Team 1, Group G1, #5, board #15)
- https://github.com/code-corhuila/opti-docs
