package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractComparatorCondition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class RegionNameCondition extends AbstractComparatorCondition {
    String  name;
    boolean group;

    public RegionNameCondition(WorldQuery.Comparator comparator, String name, boolean group) {
        super(group ? ConditionType.GROUP : ConditionType.REGION, comparator);
        this.name  = name;
        this.group = group;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return data.getRegions() == null || data.getRegions()
                .stream()
                .anyMatch(rg -> Optional.<Named>ofNullable(rg.getGroup())
                        .filter($ -> group)
                        .or(() -> Optional.of(rg))
                        .map(Named::getName)
                        .filter(it -> comparator.test(it, name))
                        .isPresent());
    }

    @Override
    protected String valueToString() {
        return name;
    }
}
