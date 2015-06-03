package com.spotify.heroic.statistics.semantic;

import lombok.ToString;

import com.spotify.heroic.statistics.LocalMetricManagerReporter;
import com.spotify.heroic.statistics.MetricBackendReporter;
import com.spotify.metrics.core.SemanticMetricRegistry;

@ToString(of = {})
public class SemanticLocalMetricManagerReporter implements LocalMetricManagerReporter {
    private final SemanticMetricRegistry registry;

    public SemanticLocalMetricManagerReporter(SemanticMetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public MetricBackendReporter newBackend(String id) {
        return new SemanticMetricBackendReporter(registry, id);
    }
}
