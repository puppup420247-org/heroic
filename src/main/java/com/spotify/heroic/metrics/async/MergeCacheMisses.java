package com.spotify.heroic.metrics.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.spotify.heroic.async.Callback;
import com.spotify.heroic.async.CancelReason;
import com.spotify.heroic.cache.AggregationCache;
import com.spotify.heroic.cache.model.CachePutResult;
import com.spotify.heroic.cache.model.CacheQueryResult;
import com.spotify.heroic.metrics.model.MetricGroup;
import com.spotify.heroic.metrics.model.MetricGroups;
import com.spotify.heroic.metrics.model.Statistics;
import com.spotify.heroic.model.DataPoint;
import com.spotify.heroic.model.TimeSerie;

@Slf4j
@RequiredArgsConstructor
final class MergeCacheMisses implements
Callback.Reducer<MetricGroups, MetricGroups> {
    @Data
    private static final class JoinResult {
        private final Map<Long, DataPoint> resultSet;
        private final Map<Long, DataPoint> countSet;
        private final Map<TimeSerie, List<DataPoint>> cacheUpdates;
        private final Statistics statistics;
    }

    private final AggregationCache cache;
    private final TimeSerie timeSerie;
    private final CacheQueryResult cacheResult;

    @Override
    public MetricGroups resolved(Collection<MetricGroups> results,
            Collection<Exception> errors, Collection<CancelReason> cancelled)
                    throws Exception {

        final MergeCacheMisses.JoinResult joinResults = joinResults(results);
        final List<MetricGroup> groups = buildDataPointGroups(joinResults);

        final Statistics statistics = joinResults.getStatistics();

        if (statistics.getRow().getFailed() == 0) {
            updateCache(joinResults.getCacheUpdates());
        } else {
            log.warn("Not updating cache because failed requests is non-zero: {}", statistics);
        }

        return new MetricGroups(groups, joinResults.getStatistics());
    }

    private List<Callback<CachePutResult>> updateCache(
            Map<TimeSerie, List<DataPoint>> cacheUpdates) {
        List<Callback<CachePutResult>> queries = new ArrayList<Callback<CachePutResult>>(
                cacheUpdates.size());

        for (Map.Entry<TimeSerie, List<DataPoint>> update : cacheUpdates
                .entrySet()) {
            final TimeSerie timeSerie = update.getKey();
            final List<DataPoint> datapoints = update.getValue();
            queries.add(cache.put(timeSerie, cacheResult.getAggregation(),
                    datapoints));
        }

        return queries;
    }

    private List<MetricGroup> buildDataPointGroups(MergeCacheMisses.JoinResult joinResult) {
        final List<MetricGroup> groups = new ArrayList<MetricGroup>();
        final List<DataPoint> datapoints = new ArrayList<DataPoint>(joinResult.getResultSet().values());
        Collections.sort(datapoints);

        groups.add(new MetricGroup(timeSerie, datapoints));
        return groups;
    }

    /**
     * Use a map from <code>{tags -> {long -> DataPoint}}</code> to
     * deduplicate overlapping datapoints.
     * 
     * These overlaps are called 'cache duplicates', which mean that we've
     * somehow managed to fetch duplicate entries from one of.
     * 
     * <ul>
     * <li>The cache backend.</li>
     * <li>The raw backends.</li>
     * </ul>
     * 
     * While this contributes to unnecessary overhead, it's not the end of
     * the world. These duplicates are reported as cacheDuplicates in
     * RowStatistics.
     */
    private MergeCacheMisses.JoinResult joinResults(Collection<MetricGroups> results) {
        final Map<TimeSerie, List<DataPoint>> cacheUpdates = new HashMap<TimeSerie, List<DataPoint>>();

        int cacheConflicts = 0;

        final Map<Long, DataPoint> resultSet = new HashMap<Long, DataPoint>();
        final Map<Long, DataPoint> countSet = new HashMap<Long, DataPoint>();

        final AddCached cachedResults = addCached(resultSet,
                cacheResult.getResult());

        Statistics statistics = Statistics.EMPTY;

        for (final MetricGroups result : results) {
            statistics = statistics.merge(result.getStatistics());

            for (final MetricGroup group : result.getGroups()) {
                for (final DataPoint d : group.getDatapoints()) {
                    if (resultSet.put(d.getTimestamp(), d) != null) {
                        cacheConflicts += 1;
                    }
                }

                cacheUpdates.put(cacheResult.getSlice().getTimeSerie(),
                        group.getDatapoints());
            }
        }

        return new JoinResult(resultSet, countSet, cacheUpdates, Statistics
                .builder(statistics)
                .cache(new Statistics.Cache(cacheConflicts, cachedResults
                        .getDuplicates(), cachedResults.getHits())).build());
    }

    @RequiredArgsConstructor
    private static final class AddCached {
        @Getter
        private final int duplicates;
        @Getter
        private final int hits;
    }
    /**
     * Add the results previously retrieved from cache.
     * 
     * @param resultGroups
     * @return
     */
    private AddCached addCached(Map<Long, DataPoint> resultSet, List<DataPoint> datapoints) {
        int duplicates = 0;
        int hits = 0;

        for (final DataPoint d : datapoints) {
            if (resultSet.put(d.getTimestamp(), d) != null) {
                duplicates += 1;
            } else {
                hits += 1;
            }
        }

        return new AddCached(duplicates, hits);
    }
}