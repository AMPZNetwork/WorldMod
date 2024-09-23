package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.mini.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record TagCondition(String data, WorldQuery.Comparator comparator) implements QueryCondition {
    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return checkContainsKeyOrValueRecursive(data.getNbt());
    }

    private boolean checkContainsKeyOrValueRecursive(@Nullable JsonNode node) {
        if (node == null)
            return SKIP;
        if (node instanceof ObjectNode obj)
            if (Streams.of(obj.fieldNames(), obj.size())
                    .anyMatch(key -> comparator.test(key, data)))
                return true;
        if (node instanceof ValueNode value)
            return comparator.test(value.asText(), data);
        for (var child : node)
            if (checkContainsKeyOrValueRecursive(child))
                return true;
        return false;
    }
}
