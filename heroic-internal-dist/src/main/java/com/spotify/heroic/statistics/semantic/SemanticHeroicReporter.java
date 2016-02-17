package com.spotify.heroic.statistics.semantic;

import com.spotify.heroic.statistics.AggregationCacheReporter;
import com.spotify.heroic.statistics.AnalyticsReporter;
import com.spotify.heroic.statistics.ClusteredManager;
import com.spotify.heroic.statistics.ClusteredMetadataManagerReporter;
import com.spotify.heroic.statistics.ClusteredMetricManagerReporter;
import com.spotify.heroic.statistics.ConsumerReporter;
import com.spotify.heroic.statistics.HeroicReporter;
import com.spotify.heroic.statistics.HttpClientManagerReporter;
import com.spotify.heroic.statistics.IngestionManagerReporter;
import com.spotify.heroic.statistics.LocalMetadataManagerReporter;
import com.spotify.heroic.statistics.LocalMetricManagerReporter;
import com.spotify.heroic.statistics.MetricBackendGroupReporter;
import com.spotify.metrics.core.SemanticMetricRegistry;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ToString(of = {})
@RequiredArgsConstructor
public class SemanticHeroicReporter implements HeroicReporter {
    private final SemanticMetricRegistry registry;

    private final Set<ClusteredManager> clusteredManagers = new HashSet<>();

    @Override
    public LocalMetricManagerReporter newLocalMetricBackendManager() {
        return new SemanticLocalMetricManagerReporter(registry);
    }

    @Override
    public LocalMetadataManagerReporter newLocalMetadataBackendManager() {
        return new SemanticMetadataManagerReporter(registry);
    }

    @Override
    public AggregationCacheReporter newAggregationCache() {
        return new SemanticAggregationCacheReporter(registry);
    }

    @Override
    public ConsumerReporter newConsumer(String id) {
        return new SemanticConsumerReporter(registry, id);
    }

    @Override
    public HttpClientManagerReporter newHttpClientManager() {
        return new SemanticHttpClientManagerReporter(registry);
    }

    @Override
    public ClusteredMetricManagerReporter newClusteredMetricBackendManager() {
        return new SemanticClusteredMetricManagerReporter(registry);
    }

    @Override
    public IngestionManagerReporter newIngestionManager() {
        return new SemanticIngestionManagerReporter(registry);
    }

    @Override
    public AnalyticsReporter newAnalyticsReporter() {
        return new SemanticAnalyticsReporter(registry);
    }

    @Override
    public ClusteredMetadataManagerReporter newClusteredMetadataBackendManager() {
        final ClusteredMetadataManagerReporter reporter =
            new SemanticClusteredMetadataManagerReporter(registry);

        synchronized (clusteredManagers) {
            clusteredManagers.add(reporter);
        }

        return reporter;
    }

    @Override
    public MetricBackendGroupReporter newMetricBackendsReporter() {
        return new SemanticMetricBackendsReporter(registry);
    }

    @Override
    public void registerShards(Set<Map<String, String>> knownShards) {
        final Set<ClusteredManager> clustered;

        synchronized (clusteredManagers) {
            clustered = new HashSet<>(clusteredManagers);
        }

        for (final ClusteredManager c : clustered) {
            c.registerShards(knownShards);
        }
    }
}
