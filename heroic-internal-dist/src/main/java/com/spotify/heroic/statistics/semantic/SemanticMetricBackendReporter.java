package com.spotify.heroic.statistics.semantic;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import com.spotify.heroic.statistics.FutureReporter;
import com.spotify.heroic.statistics.FutureReporter.Context;
import com.spotify.heroic.statistics.MetricBackendReporter;
import com.spotify.heroic.statistics.ThreadPoolReporter;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;

@ToString(of = { "base" })
@RequiredArgsConstructor
public class SemanticMetricBackendReporter implements MetricBackendReporter {
    private static final String COMPONENT = "metric-backend";

    private final MetricId base;
    private final SemanticMetricRegistry registry;
    private final FutureReporter writeBatch;
    private final FutureReporter write;
    private final FutureReporter fetch;

    public SemanticMetricBackendReporter(SemanticMetricRegistry registry, String id) {
        this.registry = registry;

        this.base = MetricId.build().tagged("component", COMPONENT, "id", id);

        this.writeBatch = new SemanticFutureReporter(registry, base.tagged("what", "write-batch", "unit", Units.WRITE));
        this.write = new SemanticFutureReporter(registry, base.tagged("what", "write", "unit", Units.WRITE));
        this.fetch = new SemanticFutureReporter(registry, base.tagged("what", "fetch", "unit", Units.READ));
    }

    @Override
    public Context reportWrite() {
        return write.setup();
    }

    @Override
    public Context reportWriteBatch() {
        return writeBatch.setup();
    }

    @Override
    public Context reportFetch() {
        return fetch.setup();
    }

    @Override
    public ThreadPoolReporter newThreadPool() {
        return new SemanticThreadPoolReporter(registry, base);
    }
}