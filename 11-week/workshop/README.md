# Week 11 Workshop - Observability on OptiView

<!-- CONFIG-START -->
- FULL_NAME: Juan Sebastian Sanchez Silva
- GITHUB_USER: jssanchezzz
- TEAM: opti (The Illusionists) - OptiView
- DOCS_REPO: https://github.com/code-corhuila/opti-docs
- REFLECTION_DOC:
- JOURNEY: Place order (quotation) - POST /api/v1/sagas/place-order through opti-api-gateway, opti-workflow, opti-customers-api, opti-products-api and opti-sales-api
<!-- CONFIG-END -->

## Why this journey

`place-order` is the only flow of OptiView that crosses every domain: the gateway, the saga
orchestrator (`opti-workflow`, ADR-004) and three domain APIs, each with its own database
(ADR-003). When a seller in `opti-sales-portal` cannot open a quotation today, the only link
between the five logs is the `X-Correlation-Id`. Observability is registered as technical
debt AT-002 in `05-architecture/overview.md`; this workshop is a first answer to it.

## Contents

| File | Checkpoint |
|---|---|
| [01-journey.md](01-journey.md) | CP1 - critical journey and trace map |
| [02-log-contract.md](02-log-contract.md) | CP2 - structured log contract |
| [03-instrumentation/](03-instrumentation/) | CP3 - trace context, logger, metrics and test (Java 21) |
| [04-metrics.md](04-metrics.md) | CP4 - golden signals and metric catalog |
