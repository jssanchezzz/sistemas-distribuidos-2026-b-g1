package co.edu.corhuila.opti.workflow.observability;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.regex.Pattern;

/** W3C trace context of the request in flight, plus the X-Correlation-Id the platform already uses. */
public record TraceContext(String traceId, String spanId, String parentSpanId, String correlationId) {

    private static final Pattern TRACEPARENT = Pattern.compile("^00-([0-9a-f]{32})-([0-9a-f]{16})-[0-9a-f]{2}$");
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    /** Continues the caller's trace when the header is valid, otherwise starts a new one. */
    public static TraceContext fromHeaders(String traceparent, String correlationId) {
        String traceId = randomHex(16);
        String parent = null;
        var match = traceparent == null ? null : TRACEPARENT.matcher(traceparent);
        if (match != null && match.matches() && !match.group(1).matches("0+")) {
            traceId = match.group(1);
            parent = match.group(2);
        }
        String correlation = correlationId != null && SAFE_ID.matcher(correlationId).matches()
                ? correlationId : traceId;
        return new TraceContext(traceId, randomHex(8), parent, correlation);
    }

    /** A new span of the same trace, for an outbound call. */
    public TraceContext child() {
        return new TraceContext(traceId, randomHex(8), spanId, correlationId);
    }

    public String traceparent() {
        return "00-" + traceId + "-" + spanId + "-01";
    }

    public static TraceContext current() {
        return CURRENT.get();
    }

    static void set(TraceContext context) {
        CURRENT.set(context);
    }

    static void clear() {
        CURRENT.remove();
    }

    private static String randomHex(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return HexFormat.of().formatHex(buffer);
    }
}
