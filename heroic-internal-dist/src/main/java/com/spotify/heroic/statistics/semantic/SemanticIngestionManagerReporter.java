package com.spotify.heroic.statistics.semantic;

import com.codahale.metrics.Counter;
import lombok.ToString;

import com.spotify.heroic.statistics.FutureReporter;
import com.spotify.heroic.statistics.FutureReporter.Context;
import com.spotify.heroic.statistics.IngestionManagerReporter;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;

@ToString(of = {})
public class SemanticIngestionManagerReporter implements IngestionManagerReporter {
    private static final String COMPONENT = "ingestion-manager";

    private final FutureReporter metadataWrite;

    private final Counter concurrentWritesCounter;

    public SemanticIngestionManagerReporter(SemanticMetricRegistry registry) {
        final MetricId id = MetricId.build().tagged("component", COMPONENT);
        this.metadataWrite = new SemanticFutureReporter(registry, id.tagged("what", "metadata-write", "unit",
                Units.FAILURE));
        this.concurrentWritesCounter = registry.counter(id.tagged("what", "concurrentWrites", "unit", Units.WRITE));
    }

    @Override
    public Context reportMetadataWrite() {
        return metadataWrite.setup();
    }

    @Override
    public void incrementConcurrentWrites() {
        concurrentWritesCounter.inc();
    }

    @Override
    public void decrementConcurrentWrites() {
        concurrentWritesCounter.dec();
    }
}
