package com.spotify.heroic.statistics.semantic;

import com.codahale.metrics.Meter;
import com.spotify.heroic.statistics.AnalyticsReporter;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import lombok.ToString;

@ToString(of = {})
public class SemanticAnalyticsReporter implements AnalyticsReporter {
    private static final String COMPONENT = "analytics";

    private final Meter droppedFetchSeries;
    private final Meter failedFetchSeries;

    public SemanticAnalyticsReporter(SemanticMetricRegistry registry) {
        final MetricId id = MetricId.build().tagged("component", COMPONENT);
        this.droppedFetchSeries =
            registry.meter(id.tagged("what", "dropped-fetch-series", "unit", Units.DROP));
        this.failedFetchSeries =
            registry.meter(id.tagged("what", "failed-fetch-series", "unit", Units.FAILURE));
    }

    @Override
    public void reportDroppedFetchSeries() {
        droppedFetchSeries.mark();
    }

    @Override
    public void reportFailedFetchSeries() {
        failedFetchSeries.mark();
    }
}
