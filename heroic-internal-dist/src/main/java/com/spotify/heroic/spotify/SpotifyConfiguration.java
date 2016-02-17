package com.spotify.heroic.spotify;

import com.spotify.heroic.HeroicConfig;
import com.spotify.heroic.HeroicCore.Builder;
import com.spotify.heroic.HeroicLifeCycle;
import com.spotify.heroic.HeroicReporterConfiguration;
import com.spotify.heroic.HeroicService;
import com.spotify.heroic.common.ServiceInfo;
import com.spotify.heroic.statistics.HeroicReporter;
import com.spotify.heroic.statistics.semantic.SemanticHeroicReporter;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import com.spotify.metrics.ffwd.FastForwardReporter;
import com.spotify.metrics.jvm.GarbageCollectorMetricSet;
import com.spotify.metrics.jvm.MemoryUsageGaugeSet;
import com.spotify.metrics.jvm.ThreadStatesMetricSet;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SpotifyConfiguration implements HeroicService.Configuration {
    @Override
    public void configure(final Builder builder, final HeroicService.Parameters params) {
        log.info("Setting up Spotify configuration");

        builder.earlyBootstrap(early -> {
            final HeroicConfig config = early.config();
            final HeroicReporterConfiguration reporterConfiguration = early.reporterConfig();
            final HeroicLifeCycle lifeCycle = early.lifeCycle();
            final ServiceInfo info = early.service();

            if (config.getDisableMetrics().orElse(false)) {
                log.warn("Not configuring metrics reporting since it is disabled");
                return;
            }

            final SemanticMetricRegistry registry = new SemanticMetricRegistry();
            final HeroicReporter reporter = new SemanticHeroicReporter(registry);

            final FastForwardReporter ffwd = setupReporter(registry, info);

            lifeCycle.registerShutdown("FastForward Reporter", () -> {
                log.info("Stopping fast forward reporter");
                ffwd.stop();
            });

            reporterConfiguration.registerReporter(reporter);
        });
    }

    private static FastForwardReporter setupReporter(
        final SemanticMetricRegistry registry, final ServiceInfo info
    ) throws IOException {
        final MetricId gauges = MetricId.build();

        registry.register(gauges, new ThreadStatesMetricSet());
        registry.register(gauges, new GarbageCollectorMetricSet());
        registry.register(gauges, new MemoryUsageGaugeSet());

        final MetricId metric =
            MetricId.build("heroic").tagged("service", "heroic", "heroic_id", info.getId());

        final FastForwardReporter ffwd = FastForwardReporter
            .forRegistry(registry)
            .schedule(TimeUnit.SECONDS, 30)
            .prefix(metric)
            .build();

        ffwd.start();

        return ffwd;
    }
}