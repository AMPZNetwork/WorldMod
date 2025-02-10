package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.ValueComparator;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.QueryCondition;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.comroid.api.data.Vector;
import org.comroid.util.MathUtil;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class PositionCondition implements QueryCondition {
    ValueComparator[] comparators;
    Vector.N3         a;
    Vector.@Nullable N3     b;

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        var position = data.getPosition();
        if (position == null)
            return true;
        if (b == null) {
            for (var i = 0; i < 3; i++)
                if (!comparators[i].test(position.get(i), a.get(i)))
                    return false;
            return true;
        }
        return MathUtil.aabb(a, b, position);
    }

    @Override
    public String toString() {
        var str = "";
        for (var dim = 0; dim < 3; dim++) {
            var v = a.get(dim);
            if (v != 0)
                str += " " + switch (dim) {
                    case 0 -> 'x';
                    case 1 -> 'y';
                    case 2 -> 'z';
                    default -> throw new IllegalStateException("Unexpected value: " + dim);
                } + '=' + v;
        }
        return str;
    }
}