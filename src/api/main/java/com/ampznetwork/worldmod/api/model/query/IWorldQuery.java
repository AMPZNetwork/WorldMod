package com.ampznetwork.worldmod.api.model.query;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.Delegate;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;

import javax.persistence.AttributeConverter;
import javax.persistence.Query;
import java.util.Optional;
import java.util.function.BiPredicate;

public interface IWorldQuery extends BiPredicate<WorldMod, QueryInputData> {
    QueryVerb getVerb();

    String getMessageKey();

    Optional<TextComponent> getMessage(WorldMod mod);

    Query toLookupQuery(WorldMod mod);

    @Value
    @javax.persistence.Converter(autoApply = true)
    class Converter implements AttributeConverter<IWorldQuery, String> {
        @Override
        public String convertToDatabaseColumn(IWorldQuery query) {
            return query.toString();
        }

        @Override
        public IWorldQuery convertToEntityAttribute(String query) {
            return Delegate.INSTANCE.parseQuery(query);
        }
    }
}
