package com.spotify.heroic.statistics.semantic;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import com.spotify.heroic.statistics.FutureReporter;
import com.spotify.heroic.statistics.MetricBackendGroupReporter;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;

@ToString(of = {})
@RequiredArgsConstructor
public class SemanticMetricBackendsReporter implements MetricBackendGroupReporter {
    private static final String COMPONENT = "metric-backends";

    private final FutureReporter query;
    private final FutureReporter write;
    private final FutureReporter writeBatch;
    private final FutureReporter queryMetrics;
    private final FutureReporter findSeries;

    public SemanticMetricBackendsReporter(SemanticMetricRegistry registry) {
        final MetricId id = MetricId.build().tagged("component", COMPONENT);

        this.query = new SemanticFutureReporter(registry, id.tagged("what", "query", "unit", Units.READ));
        this.write = new SemanticFutureReporter(registry, id.tagged("what", "write", "unit", Units.WRITE));
        this.writeBatch = new SemanticFutureReporter(registry, id.tagged("what", "write-batch", "unit", Units.WRITE));
        this.queryMetrics = new SemanticFutureReporter(registry, id.tagged("what", "query-metrics", "unit", Units.READ));
        this.findSeries = new SemanticFutureReporter(registry, id.tagged("what", "find-series", "unit", Units.LOOKUP));
    }

    @Override
    public FutureReporter.Context reportQuery() {
        return query.setup();
    }

    @Override
    public FutureReporter.Context reportWrite() {
        return write.setup();
    }

    @Override
    public FutureReporter.Context reportWriteBatch() {
        return writeBatch.setup();
    }

    @Override
    public FutureReporter.Context reportQueryMetrics() {
        return queryMetrics.setup();
    }

    @Override
    public FutureReporter.Context reportFindSeries() {
        return findSeries.setup();
    }
}