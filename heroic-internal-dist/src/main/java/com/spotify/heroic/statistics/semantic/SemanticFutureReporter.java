package com.spotify.heroic.statistics.semantic;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import com.codahale.metrics.Meter;
import com.spotify.heroic.statistics.FutureReporter;
import com.spotify.heroic.statistics.HeroicTimer;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;

@ToString(of = {})
public class SemanticFutureReporter implements FutureReporter {
    private final SemanticHeroicTimer timer;
    private final Meter failed;
    private final Meter resolved;
    private final Meter cancelled;

    public SemanticFutureReporter(SemanticMetricRegistry registry, MetricId id) {
        final String what = id.getTags().get("what");

        if (what == null)
            throw new IllegalArgumentException("id does not provide the tag 'what'");

        this.timer = new SemanticHeroicTimer(registry.timer(id.tagged("what", what + "-latency")));
        this.failed = registry.meter(id.tagged("what", what + "-failure-rate", "unit", Units.FAILURE));
        this.resolved = registry.meter(id.tagged("what", what + "-resolve-rate", "unit", Units.RESOLVE));
        this.cancelled = registry.meter(id.tagged("what", what + "-cancel-rate", "unit", Units.CANCEL));
    }

    @Override
    public FutureReporter.Context setup() {
        return new SemanticContext(timer.time());
    }

    @RequiredArgsConstructor
    private class SemanticContext implements FutureReporter.Context {
        private final HeroicTimer.Context context;

        @Override
        public void failed(Throwable e) throws Exception {
            failed.mark();
            context.stop();
        }

        @Override
        public void resolved(Object result) throws Exception {
            resolved.mark();
            context.stop();
        }

        @Override
        public void cancelled() throws Exception {
            cancelled.mark();
            context.stop();
        }
    }
}