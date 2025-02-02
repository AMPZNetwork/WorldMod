package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractComparatorCondition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class TagCondition extends AbstractComparatorCondition {
    String data;

    public TagCondition(WorldQuery.Comparator comparator, String data) {
        super(ConditionType.TAG, comparator);
        this.data = data;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return checkContainsKeyOrValueRecursive(data.getNbt());
    }

    private boolean checkContainsKeyOrValueRecursive(@Nullable JsonNode node) {
        if (node == null) return SKIP;
        if (node instanceof ObjectNode obj) if (Streams.of(obj.fieldNames(), obj.size()).anyMatch(key -> comparator.test(key, data))) return true;
        if (node instanceof ValueNode value) return comparator.test(value.asText(), data);
        for (var child : node)
            if (checkContainsKeyOrValueRecursive(child)) return true;
        return false;
    }

    @Override
    protected String valueToString() {
        return data;
    }
}
