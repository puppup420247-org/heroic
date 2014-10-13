package com.spotify.heroic.aggregation;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotify.heroic.model.DataPoint;
import com.spotify.heroic.model.DateRange;
import com.spotify.heroic.model.Sampling;
import com.spotify.heroic.model.Statistics;

@Data
public class AggregationGroup {
    private static final class Session implements Aggregation.Session {
        private final Aggregation.Session first;
        private final Iterable<Aggregation.Session> rest;

        public Session(Aggregation.Session first, Iterable<Aggregation.Session> rest) {
            this.first = first;
            this.rest = rest;
        }

        @Override
        public void update(Iterable<DataPoint> datapoints) {
            first.update(datapoints);
        }

        @Override
        public Aggregation.Result result() {
            final Aggregation.Result firstResult = first.result();
            List<DataPoint> datapoints = firstResult.getResult();
            Statistics.Aggregator statistics = firstResult.getStatistics();

            for (final Aggregation.Session session : rest) {
                session.update(datapoints);
                final Aggregation.Result next = session.result();
                datapoints = next.getResult();
                statistics = statistics.merge(next.getStatistics());
            }

            return new Aggregation.Result(datapoints, statistics);
        }
    }

    private final List<Aggregation> aggregations;
    private final Sampling sampling;

    public Aggregation.Session session(DateRange range) {
        final Aggregation.Session first = aggregations.get(0).session(range);
        final List<Aggregation.Session> rest = new ArrayList<Aggregation.Session>();

        for (final Aggregation aggregator : aggregations.subList(1, aggregations.size())) {
            rest.add(aggregator.session(range));
        }

        return new Session(first, rest);
    }

    @JsonCreator
    public static AggregationGroup create(
            @JsonProperty(value = "aggregations", required = true) List<Aggregation> aggregations,
            @JsonProperty(value = "sampling", required = true) Sampling sampling) {
        return new AggregationGroup(aggregations, sampling);
    }
}