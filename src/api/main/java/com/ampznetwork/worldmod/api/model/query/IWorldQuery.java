package com.ampznetwork.worldmod.api.model.query;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.delegate.ModDelegate;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Query;
import lombok.Value;

import java.util.function.BiPredicate;

public interface IWorldQuery extends BiPredicate<WorldMod, QueryInputData> {
    QueryVerb getVerb();

    String getMessageKey();

    Query toLookupQuery(WorldMod mod);

    @Value
    @jakarta.persistence.Converter(autoApply = true)
    class Converter implements AttributeConverter<IWorldQuery, String> {
        @Override
        public String convertToDatabaseColumn(IWorldQuery query) {
            return query.toString();
        }

        @Override
        public IWorldQuery convertToEntityAttribute(String query) {
            return ModDelegate.INSTANCE.parseQuery(query);
        }
    }
}
