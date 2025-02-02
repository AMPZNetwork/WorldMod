package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractComparatorCondition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class BlockTypeCondition extends AbstractComparatorCondition {
    String identifier;

    public BlockTypeCondition(WorldQuery.Comparator comparator, String identifier) {
        super(ConditionType.TYPE, comparator);
        this.identifier = identifier;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        var targetResourceKey = data.getTargetResourceKey();
        return targetResourceKey == null || (comparator.test(identifier.contains(":") ? targetResourceKey.asString() : targetResourceKey.value(), identifier));
    }

    @Override
    protected String valueToString() {
        return identifier;
    }
}
