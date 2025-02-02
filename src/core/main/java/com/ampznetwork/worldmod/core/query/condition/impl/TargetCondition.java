package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractComparatorCondition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class TargetCondition extends AbstractComparatorCondition {
    public static final String TAG = "#";
    String target;

    public TargetCondition(WorldQuery.Comparator comparator, String target) {
        super(ConditionType.TARGET, comparator);
        this.target = target;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        var player = data.getPlayer();
        var tagged = target.startsWith(TAG);
        return !tagged && player == null || comparator.test(tagged ? data.getNonPlayerSource() : Optional.of(player).map(Player::getName).orElse(null), target);
    }

    @Override
    protected String valueToString() {
        return target;
    }
}
