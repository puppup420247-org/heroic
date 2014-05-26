package com.spotify.heroic.aggregator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.spotify.heroic.aggregation.Aggregation;
import com.spotify.heroic.aggregation.AggregationGroup;
import com.spotify.heroic.aggregation.AggregationGroupSerializer;
import com.spotify.heroic.aggregation.SumAggregation;
import com.spotify.heroic.model.Resolution;

public class AggregationGroupSerializerTest {
    private static final AggregationGroupSerializer serializer = AggregationGroupSerializer.get();
    private static final Resolution resolution = new Resolution(Resolution.DEFAULT_UNIT, Resolution.DEFAULT_VALUE);

    private AggregationGroup roundTrip(AggregationGroup aggregationGroup) {
        return serializer.fromByteBuffer(serializer.toByteBuffer(aggregationGroup));
    }

    @Test
    public void testEmpty() throws Exception {
        final AggregationGroup aggregationGroup = new AggregationGroup(new ArrayList<Aggregation>());
        Assert.assertEquals(aggregationGroup, roundTrip(aggregationGroup));
    }

    @Test
    public void testSome() throws Exception {
        final Aggregation aggregation = new SumAggregation(resolution);
        final List<Aggregation> aggregations = new ArrayList<Aggregation>();
        aggregations.add(aggregation);
        final AggregationGroup aggregationGroup = new AggregationGroup(aggregations);
        Assert.assertEquals(aggregationGroup, roundTrip(aggregationGroup));
    }
}
