package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.ValueComparator;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractComparatorCondition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class WorldCondition extends AbstractComparatorCondition {
    String[] worlds;

    public WorldCondition(ValueComparator comparator, String... worlds) {
        super(ConditionType.WORLD, comparator);
        this.worlds = worlds;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return Arrays.stream(worlds).anyMatch(world -> comparator.test(data.getWorldName(), world));
    }

    @Override
    protected String valueToString() {
        return String.join(",", worlds);
    }
}
