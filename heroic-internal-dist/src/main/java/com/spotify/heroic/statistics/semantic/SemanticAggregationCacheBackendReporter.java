package com.spotify.heroic.statistics.semantic;

import lombok.ToString;

import com.spotify.heroic.statistics.AggregationCacheBackendReporter;
import com.spotify.heroic.statistics.ThreadPoolReporter;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;

@ToString(of={"base"})
public class SemanticAggregationCacheBackendReporter implements AggregationCacheBackendReporter {
    private static final String COMPONENT = "aggregation-cache-backend";
    private final SemanticMetricRegistry registry;
    private final MetricId base;

    public SemanticAggregationCacheBackendReporter(SemanticMetricRegistry registry) {
        this.registry = registry;
        this.base = MetricId.build().tagged("component", COMPONENT);
    }

    @Override
    public ThreadPoolReporter newThreadPool() {
        return new SemanticThreadPoolReporter(registry, base);
    }
}
