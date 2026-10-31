# Week 13 Workshop - Threat modeling on OptiView

<!-- CONFIG-START -->
- FULL_NAME: Juan Sebastian Sanchez Silva
- GITHUB_USER: jssanchezzz
- TEAM: opti (The Illusionists) - OptiView
- DOCS_REPO: https://github.com/code-corhuila/opti-docs
- BOUNDARY: TB4 - opti-sales-api to the external payment API (Wompi)
- CLAIM_ISSUE: https://github.com/code-corhuila/opti-docs/issues/59
- STRIDE_PR: https://github.com/code-corhuila/opti-docs/pull/60
- REFLECTION_DOC:
- LIBRARY_BOOK: Tanenbaum, A. S. and van Steen, M. Sistemas distribuidos: principios y paradigmas. 2nd ed. Pearson Educacion, 2008. Call number 0003771 (CORHUILA Prado Alto library). Chapter 9, pp. 378-381, 396-397, 413-414, 428
- LIBRARY_RECORD_TIME: 2026-10-28 19:10
- STORIES: https://github.com/code-corhuila/opti-docs/issues/61, https://github.com/code-corhuila/opti-docs/issues/62, https://github.com/code-corhuila/opti-docs/issues/63
<!-- CONFIG-END -->

## Why this boundary

TB4 is the only point where OptiView sends data outside its own Docker network. Inside
`POST /api/v1/invoices/{id}/payments`, `opti-sales-api` calls Wompi with `WOMPI_PRIVATE_KEY` as a
bearer token and sends the customer's Nequi phone number and the payment amount. A leaked key or a
tampered amount here means real money and personal data, so it is the boundary with the highest
impact in the sales domain. No other member of the team claimed it.

## Contents

| File | Checkpoint |
|---|---|
| [01-attack-surface.md](01-attack-surface.md) | CP1 - data flow, trust boundaries, assets and entry points |
| [04-security-ci/](04-security-ci/) | CP4 / CP8 - security gates, negative authorization test and how to run it |
| `opti-docs` `05-architecture/security/stride-jssanchezzz.md` | CP2 / CP6 - STRIDE table and risk ranking |
