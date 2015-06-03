package com.spotify.heroic;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.google.inject.Injector;
import com.spotify.heroic.HeroicCore.Builder;
import com.spotify.heroic.HeroicService.Parameters;
import com.spotify.heroic.statistics.HeroicReporter;
import com.spotify.heroic.statistics.semantic.SemanticHeroicReporter;
import com.spotify.metrics.core.MetricId;
import com.spotify.metrics.core.SemanticMetricRegistry;
import com.spotify.metrics.ffwd.FastForwardReporter;
import com.spotify.metrics.jvm.GarbageCollectorMetricSet;
import com.spotify.metrics.jvm.MemoryUsageGaugeSet;
import com.spotify.metrics.jvm.ThreadStatesMetricSet;

@Slf4j
public class HeroicInternalService {
    public static void main(String[] args) throws Exception {
        HeroicService.main(args, new HeroicService.Configuration() {
            @Override
            public void configure(final Builder builder, final Parameters params) {
                final SemanticMetricRegistry registry = new SemanticMetricRegistry();
                final HeroicReporter reporter = new SemanticHeroicReporter(registry);

                builder.reporter(reporter).configurator(new HeroicConfigurator() {
                    @Override
                    public void setup(final Injector injector) throws Exception {
                        final HeroicInternalLifeCycle lifecycle = injector.getInstance(HeroicInternalLifeCycle.class);
                        final FastForwardReporter ffwd = setupReporter(registry, params.getId());

                        lifecycle.registerShutdown("FastForward Reporter", new HeroicInternalLifeCycle.ShutdownHook() {
                            @Override
                            public void onShutdown() {
                                log.info("Stopping fast forward reporter");
                                ffwd.stop();
                            }
                        });
                    }
                });
            }
        });
    }

    private static FastForwardReporter setupReporter(final SemanticMetricRegistry registry, final String id)
            throws IOException {
        final MetricId gauges = MetricId.build();

        registry.register(gauges, new ThreadStatesMetricSet());
        registry.register(gauges, new GarbageCollectorMetricSet());
        registry.register(gauges, new MemoryUsageGaugeSet());

        final MetricId metric = MetricId.build("heroic").tagged("service", "heroic", "heroic_id",
                id == null ? "none" : id);

        final FastForwardReporter ffwd = FastForwardReporter.forRegistry(registry).schedule(TimeUnit.SECONDS, 30)
                .prefix(metric).build();

        ffwd.start();

        return ffwd;
    }
}