# W11 Reflection - Observability on OptiView

**CP0 - My part.** I own `opti-workflow`, the orchestrator of the `place-order` saga. Observability
is registered as technical debt AT-002 in `05-architecture/overview.md`, and this week was our
first real step to pay it.

**CP1 - Journey and trace map.** I learned that a trace is one whole request, a span is one hop
inside it, and the `trace_id` travels between services in the W3C `traceparent` header. In our
saga the request goes gateway -> opti-workflow -> customers-api -> products-api -> sales-api, but
today the only shared id is `X-Correlation-Id`. The hard part is the async boundary: when a
participant is down, the saga stays in Redis and the resume task finishes it after 30 s, so the
`traceparent` must be stored in the saga state to keep the same trace. The first thing I would do
is add `traceparent` to `CorrelationFilter` and `ParticipantClient`; what stops us from tracing the
`OrdenCreada` event is that the broker is not chosen yet.

**CP2 - Log contract.** I learned that if every service writes the same JSON fields, one query by
`trace_id` tells the whole story. In my example, products-api did not answer within the 5 s
`HTTP_TIMEOUT`, the retry got `INSUFFICIENT_STOCK`, and the saga ended `COMPENSATED`; seven lines
from three services explain it without opening any code. I also learned what must never be logged:
`document_number`, names, phone, email, the optical formula and tokens; we use `patient_id`
instead. My first step would be to rename `correlationId` to `correlation_id` in the
`LogstashEncoder` and agree on the event names with the team.

**CP3 - Instrumentation code.** I learned that observability is code: a filter that reads the
context on the way in, a header that carries it on the way out, and metrics. My test proves that
the `trace_id` sent by the gateway reaches customers-api inside a new span. When I ran `mvn test`,
_[write here what broke and how you fixed it, e.g. a wrong Java version, a failing assertion or a
port already in use]_. The blocker in the real services is that the actuator endpoints are
disabled (`enabled-by-default: false`).

**CP4 - Golden signals.** I learned the four signals (latency, traffic, errors, saturation) and
that labels must be bounded. That is why we use the route template instead of the raw URL and
never put `patient_id` or `sku` as a label; our biggest metric has about 45 series. The latency
buckets go up to 10 s with a cut at 5 s, the workflow timeout, because a seller at the counter
expects the quotation in under 1 s. The first thing I would implement is Micrometer + Prometheus
on `opti-workflow`.
