package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.libmod.api.entity.Player;
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
import java.util.stream.Stream;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class SourceCondition extends AbstractComparatorCondition {
    String[] sources;

    public SourceCondition(ValueComparator comparator, String... sources) {
        super(ConditionType.SOURCE, comparator);
        this.sources = sources;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return Stream.concat(Stream.ofNullable(data.getPlayer()).map(Player::getName), Stream.ofNullable(data.getNonPlayerSource()).map(Object::toString))
                .anyMatch(str -> Arrays.stream(sources).anyMatch(source -> comparator.test(str, source)));
    }

    @Override
    protected String valueToString() {
        return String.join(",", sources);
    }
}
