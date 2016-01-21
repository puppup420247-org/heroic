package com.spotify.heroic.aggregation;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Backwards compatibility from the dark ages, where group aggregations 'each' field took a list.
 *
 * XXX: remove when deprecated.
 *
 * @author udoprog
 */
@JsonDeserialize(using = AggregationOrList.Deserializer.class)
public class AggregationOrList {
    private final Optional<Aggregation> aggregation;

    public AggregationOrList(final Optional<Aggregation> aggregation) {
        this.aggregation = aggregation;
    }

    @JsonValue
    public Optional<Aggregation> toAggregation() {
        return aggregation;
    }

    public static final class Deserializer extends JsonDeserializer<AggregationOrList> {
        private static final TypeReference<List<Aggregation>> LIST_OF_AGGREGATIONS =
                new TypeReference<List<Aggregation>>() {
                };

        @Override
        public AggregationOrList deserialize(final JsonParser p, final DeserializationContext c)
                throws IOException, JsonProcessingException {
            switch (p.getCurrentToken()) {
            case START_ARRAY:
                final List<Aggregation> chain = p.readValueAs(LIST_OF_AGGREGATIONS);
                return new AggregationOrList(Aggregations.chain(chain));
            case START_OBJECT:
                return new AggregationOrList(Optional.of(p.readValueAs(Aggregation.class)));
            default:
                throw c.wrongTokenException(p, JsonToken.START_OBJECT, null);
            }
        }
    }
}
