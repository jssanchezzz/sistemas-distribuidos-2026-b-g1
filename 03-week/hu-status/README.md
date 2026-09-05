# Weekly Status - Week 03

<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.

```
 Your weekly grade is read AUTOMATICALLY from this file:
   02-week/hu-status/README.md  (inside YOUR fork). English. -->
```

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->

* FULL_NAME: Juan Sebastian Sanchez Silva
* GITHUB_USER: jssanchezzz
* TEAM: The illusionists
* SPRINT_GOAL: Generation of first HUs in GitHub Projects (HU10-HU08) and creation of the test repository for the correct use of the Git-GitHub platform, including branches, commits, and cherry-picks

<!-- CONFIG-END -->

## 1. User stories worked this week

| HU ID      | Title                                                                            | Status (todo/doing/done) | Evidence (PR or commit URL)                                                |
| ---------- | -------------------------------------------------------------------------------- | ------------------------ | -------------------------------------------------------------------------- |
| HU-XXX-001 | OptiView System Landscape, Ubiquitous Language and Single Database Documentation | done                     | `docs(domain): document optiview landscape, language, and single database` |

## 2. My individual contribution

During Week 02, I contributed to the OptiView project by documenting the **system landscape, ubiquitous language, and single-database architecture** required for the domain model.

The documentation was created in both **English and Spanish**, providing a consistent reference for the team's understanding of the system topology, terminology, and database organization.

### Main activities completed

* Documented the **OptiView system landscape**, describing the course topology with:

  * Four frontend applications.
  * Four backend applications.
  * Three environments.
* Documented the **single PostgreSQL database architecture** and its schema organization.
* Defined the structure and relationships of the database schemas used by the different system components.
* Created and documented the **OptiView ubiquitous language**, establishing consistent terminology for the domain and technical documentation.
* Created the documentation in **English and Spanish**.
* Added the corresponding English documentation:

  * `02-domain/optiview/en/README.md`
  * `02-domain/optiview/en/ubiquitous-language.md`
  * `02-domain/optiview/en/system-landscape.md`
  * `02-domain/optiview/en/single-database.md`
* Added the corresponding Spanish documentation:

  * `02-domain/optiview/es/README.md`
  * `02-domain/optiview/es/lenguaje-ubiquo.md`
  * `02-domain/optiview/es/paisaje-del-sistema.md`
  * `02-domain/optiview/es/base-de-datos-unica.md`
* Organized the documentation so that the domain README files provide access to the corresponding artifacts.

### Commit

The contribution was implemented in the following commit:

`docs(domain): document optiview landscape, language, and single database`

This commit establishes a shared reference for the OptiView system topology, ubiquitous language, and single PostgreSQL database structure in both supported languages.

## 3. Blockers and risks

* No blockers were identified during the documentation process.
* A potential risk is that future changes to the system topology, backend/frontend distribution, or database structure may require the domain documentation to be updated accordingly.
* The ubiquitous language should remain consistent across the documentation and future implementation.

## 4. Plan for next week

* Review the documented system landscape and database structure with the team.
* Validate that the documented terminology is consistent with the user stories and domain model.
* Continue refining the OptiView domain documentation based on the team's feedback.
* Begin connecting the documented domain concepts with the implementation requirements of the user stories.

## 5. Compliance self-check

* [x] Conventional Commits - `type(scope): summary`
* [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
* [ ] Testable acceptance criteria
* [ ] Tests added/updated (unit / integration)
* [x] DDD / hexagonal boundaries respected (domain has no I/O)
* [x] No secrets; config via environment variables

## 6. Evidence links

* Commit: `docs(domain): document optiview landscape, language, and single database`
* English domain documentation: `02-domain/optiview/en/`
* Spanish domain documentation: `02-domain/optiview/es/`
* System landscape: `system-landscape.md` / `paisaje-del-sistema.md`
* Single database: `single-database.md` / `base-de-datos-unica.md`
* Ubiquitous language: `ubiquitous-language.md` / `lenguaje-ubiquo.md`
