package com.ampznetwork.worldmod.api.model.query;

import com.ampznetwork.worldmod.api.WorldMod;
import net.kyori.adventure.text.TextComponent;

import javax.persistence.Query;
import java.util.Optional;
import java.util.function.BiPredicate;

public interface IWorldQuery extends BiPredicate<WorldMod, QueryInputData> {
    QueryVerb getVerb();

    String getMessageKey();

    Optional<TextComponent> getMessage(WorldMod mod);

    Query toLookupQuery(WorldMod mod);
}
