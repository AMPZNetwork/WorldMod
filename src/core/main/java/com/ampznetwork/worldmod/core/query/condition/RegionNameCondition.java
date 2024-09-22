package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.core.query.InputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record RegionNameCondition(String name, boolean group, WorldQuery.Comparator comparator) implements QueryCondition {
    @Override
    public boolean test(WorldMod mod, WorldQuery query, InputData data, @Nullable UUID executor) {
        return data.getRegions() == null || data.getRegions().stream()
                .anyMatch(rg -> Optional.<Named>ofNullable(rg.getGroup())
                        .filter($ -> group)
                        .or(() -> Optional.of(rg))
                        .map(Named::getName)
                        .filter(it -> comparator.test(name, it))
                        .isPresent());
    }
}
