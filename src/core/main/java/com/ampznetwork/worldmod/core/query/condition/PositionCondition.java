package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.core.query.InputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import lombok.Value;
import org.comroid.api.data.Vector;
import org.comroid.util.MathUtil;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Value
public class PositionCondition implements QueryCondition {
    WorldQuery.Comparator[] comparators;
    Vector.N3               a;
    Vector.@Nullable N3     b;

    @Override
    public boolean test(WorldMod mod, WorldQuery query, InputData data, @Nullable UUID executor) {
        var position = data.getPosition();
        if (position == null)
            return true;
        if (b == null) {
            for (var i = 0; i < 3; i++)
                if (!comparators[i].test(a.get(i), position.get(i)))
                    return false;
            return true;
        }
        return MathUtil.aabb(a, b, position);
    }
}