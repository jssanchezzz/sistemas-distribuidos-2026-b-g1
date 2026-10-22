package co.edu.corhuila.opti.workflow.observability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * The two metrics of 04-metrics.md for opti-workflow, written in the Prometheus text format:
 * a counter of sagas by outcome and a latency histogram of the place-order request.
 */
public class WorkflowMetrics {

    static final double[] BUCKETS = {0.1, 0.25, 0.5, 1, 2, 3, 5, 10};

    private final Map<String, AtomicLong> sagas = new ConcurrentHashMap<>();
    private final AtomicLong[] bucketCounts = new AtomicLong[BUCKETS.length];
    private final AtomicLong count = new AtomicLong();
    private final DoubleAdder sum = new DoubleAdder();

    public WorkflowMetrics() {
        for (int i = 0; i < BUCKETS.length; i++) {
            bucketCounts[i] = new AtomicLong();
        }
    }

    /** outcome is a closed list: completed, compensated, failed. */
    public void sagaFinished(String outcome) {
        sagas.computeIfAbsent(outcome, k -> new AtomicLong()).incrementAndGet();
    }

    public void requestDuration(double seconds) {
        for (int i = 0; i < BUCKETS.length; i++) {
            if (seconds <= BUCKETS[i]) {
                bucketCounts[i].incrementAndGet();
            }
        }
        count.incrementAndGet();
        sum.add(seconds);
    }

    public String scrape() {
        StringBuilder out = new StringBuilder();
        out.append("# HELP workflow_sagas_total Sagas finished, by type and outcome.\n");
        out.append("# TYPE workflow_sagas_total counter\n");
        sagas.forEach((outcome, n) -> out.append("workflow_sagas_total{saga_type=\"place-order\",outcome=\"")
                .append(outcome).append("\"} ").append(n.get()).append('\n'));
        String labels = "route=\"/api/v1/sagas/place-order\",method=\"POST\"";
        out.append("# HELP workflow_http_request_duration_seconds Latency of the place-order request.\n");
        out.append("# TYPE workflow_http_request_duration_seconds histogram\n");
        for (int i = 0; i < BUCKETS.length; i++) {
            out.append("workflow_http_request_duration_seconds_bucket{").append(labels).append(",le=\"")
                    .append(BUCKETS[i]).append("\"} ").append(bucketCounts[i].get()).append('\n');
        }
        out.append("workflow_http_request_duration_seconds_bucket{").append(labels).append(",le=\"+Inf\"} ")
                .append(count.get()).append('\n');
        out.append("workflow_http_request_duration_seconds_sum{").append(labels).append("} ").append(sum.sum()).append('\n');
        out.append("workflow_http_request_duration_seconds_count{").append(labels).append("} ").append(count.get()).append('\n');
        return out.toString();
    }
}
