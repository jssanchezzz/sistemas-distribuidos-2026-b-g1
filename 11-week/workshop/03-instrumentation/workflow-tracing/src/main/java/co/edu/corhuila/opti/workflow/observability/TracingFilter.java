package co.edu.corhuila.opti.workflow.observability;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * Inbound side: reads or creates the trace context, returns it in the response and writes one
 * access line in the log contract. Same job as CorrelationFilter in opti-workflow, plus traceparent.
 */
public class TracingFilter extends Filter {

    private final JsonLog log;

    public TracingFilter(JsonLog log) {
        this.log = log;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        TraceContext ctx = TraceContext.fromHeaders(exchange.getRequestHeaders().getFirst("traceparent"),
                exchange.getRequestHeaders().getFirst("X-Correlation-Id"));
        TraceContext.set(ctx);
        exchange.getResponseHeaders().set("traceparent", ctx.traceparent());
        exchange.getResponseHeaders().set("X-Correlation-Id", ctx.correlationId());
        long started = System.nanoTime();
        try {
            chain.doFilter(exchange);
        } finally {
            long millis = (System.nanoTime() - started) / 1_000_000;
            log.info("http.request.completed", exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath(),
                    millis, Map.of("route", exchange.getHttpContext().getPath(),
                            "http_status", exchange.getResponseCode()));
            TraceContext.clear();
        }
    }

    @Override
    public String description() {
        return "W3C trace context + structured access log";
    }
}
