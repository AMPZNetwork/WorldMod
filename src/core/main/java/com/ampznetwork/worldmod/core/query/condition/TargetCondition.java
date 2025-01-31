package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.mini.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record TargetCondition(String target, WorldQuery.Comparator comparator) implements QueryCondition {
    public static final String TAG = "#";

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        var player = data.getPlayer();
        var tagged = target.startsWith(TAG);
        return !tagged && player == null || comparator.test(tagged ? data.getNonPlayerSource() : Optional.of(player).map(Player::getName).orElse(null), target);
    }
}
