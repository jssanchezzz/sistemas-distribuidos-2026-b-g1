package co.edu.corhuila.opti.workflow.observability;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Writes one JSON line per event following 02-log-contract.md; trace fields come from the context. */
public final class JsonLog {

    private final String service;
    private final String version;
    private final String environment;
    private final Consumer<String> sink;

    public JsonLog(String service, String version, String environment, Consumer<String> sink) {
        this.service = service;
        this.version = version;
        this.environment = environment;
        this.sink = sink;
    }

    public void info(String event, String message, long durationMs, Map<String, Object> domain) {
        write("INFO", event, message, durationMs, domain);
    }

    public void warn(String event, String message, long durationMs, Map<String, Object> domain) {
        write("WARN", event, message, durationMs, domain);
    }

    public void error(String event, String message, long durationMs, Map<String, Object> domain) {
        write("ERROR", event, message, durationMs, domain);
    }

    private void write(String level, String event, String message, long durationMs, Map<String, Object> domain) {
        TraceContext ctx = TraceContext.current();
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("timestamp", Instant.now().toString());
        line.put("level", level);
        line.put("service", service);
        line.put("version", version);
        line.put("environment", environment);
        line.put("trace_id", ctx == null ? null : ctx.traceId());
        line.put("span_id", ctx == null ? null : ctx.spanId());
        line.put("correlation_id", ctx == null ? null : ctx.correlationId());
        line.put("event", event);
        line.put("message", message);
        line.put("duration_ms", durationMs);
        line.putAll(domain);
        sink.accept(toJson(line));
    }

    static String toJson(Map<String, Object> fields) {
        StringBuilder out = new StringBuilder("{");
        fields.forEach((key, value) -> {
            if (out.length() > 1) {
                out.append(',');
            }
            out.append('"').append(key).append("\":");
            if (value == null) {
                out.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                out.append(value);
            } else {
                out.append('"').append(value.toString().replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
        });
        return out.append('}').toString();
    }
}
