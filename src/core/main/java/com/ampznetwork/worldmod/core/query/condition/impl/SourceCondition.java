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
public class SourceCondition extends AbstractComparatorCondition {
    public static final String TAG = "#";
    String source;

    public SourceCondition(WorldQuery.Comparator comparator, String source) {
        super(ConditionType.SOURCE, comparator);
        this.source = source;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        var player = data.getPlayer();
        var tagged = source.startsWith(TAG);
        return !tagged && player == null || comparator.test(tagged ? data.getNonPlayerSource() : Optional.of(player).map(Player::getName).orElse(null), source);
    }

    @Override
    protected String valueToString() {
        return source;
    }
}
