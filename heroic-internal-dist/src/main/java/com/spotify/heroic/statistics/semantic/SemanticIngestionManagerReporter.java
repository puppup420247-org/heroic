package com.spotify.heroic.statistics.semantic;

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

    public SemanticIngestionManagerReporter(SemanticMetricRegistry registry) {
        final MetricId id = MetricId.build().tagged("component", COMPONENT);
        this.metadataWrite = new SemanticFutureReporter(registry, id.tagged("what", "metadata-write", "unit",
                Units.FAILURE));
    }

    @Override
    public Context reportMetadataWrite() {
        return metadataWrite.setup();
    }
}