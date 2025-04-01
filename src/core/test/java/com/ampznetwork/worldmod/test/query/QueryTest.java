package com.ampznetwork.worldmod.test.query;

import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.model.query.QueryVerb;
import com.ampznetwork.worldmod.core.query.ValueComparator;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.QueryCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.BlockTypeCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.FlagCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.PositionCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.RadiusCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.RegionNameCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.SourceCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.WorldCondition;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class QueryTest {
    @Test
    void test1() {
        var query = WorldQuery.parse("lookup from=fire radius=20");
        assertEquals(QueryVerb.LOOKUP, query.getVerb());
        assertEquals(2, query.getConditions().size());
        assertCondition(query, SourceCondition.class, sourceCondition -> sourceCondition.getSources()[0], "fire", ValueComparator.EQUALS);
        assertCondition(query, RadiusCondition.class, RadiusCondition::getRadius, 20, ValueComparator.EQUALS);
    }

    @Test
    void test2() {
        var query = WorldQuery.parse("lookup x=10..20 z=30..50 since=2w");
        assertEquals(QueryVerb.LOOKUP, query.getVerb());
        assertEquals(2, query.getConditions().size());
        assertCondition(query, PositionCondition.class, PositionCondition::getA, new Vector.N3(10, 0, 30), ValueComparator.EQUALS);
        assertCondition(query, PositionCondition.class, PositionCondition::getB, new Vector.N3(20, 0, 50), ValueComparator.EQUALS);
        //assertCondition(query, TimeCondition.class, TimeCondition::since, );
    }

    @Test
    void test3() {
        var query = WorldQuery.parse("lookup from=Kaleidox x=0 y=0 z=0 world=world type=minecraft:iron_block");
        assertEquals(QueryVerb.LOOKUP, query.getVerb());
        assertEquals(4, query.getConditions().size());
        assertCondition(query, SourceCondition.class, sourceCondition -> sourceCondition.getSources()[0], "Kaleidox", ValueComparator.EQUALS);
        assertCondition(query, WorldCondition.class, worldCondition -> worldCondition.getWorlds()[0], "world", ValueComparator.EQUALS);
        assertCondition(query, BlockTypeCondition.class, blockTypeCondition -> blockTypeCondition.getIdentifiers()[0], "minecraft:iron_block",
                ValueComparator.EQUALS);
    }

    @Test
    void test4() {
        var query = WorldQuery.parse("allow tag~=grave");
        assertEquals(QueryVerb.ALLOW, query.getVerb());
        assertEquals(1, query.getConditions().size());
        //assertCondition(query, );
    }

    @Test
    void test5() {
        var query = WorldQuery.parse("deny region=spawn");
        assertEquals(QueryVerb.DENY, query.getVerb());
        assertEquals(1, query.getConditions().size());
        assertCondition(query, RegionNameCondition.class, regionNameCondition -> regionNameCondition.getNames()[0], "spawn", ValueComparator.EQUALS);
        assertCondition(query, RegionNameCondition.class, RegionNameCondition::isGroup, false);
    }

    @Test
    void test6() {
        var query = WorldQuery.parse("deny type=minecraft:iron_block flag=build");
        assertEquals(QueryVerb.DENY, query.getVerb());
        assertEquals(2, query.getConditions().size());
        assertCondition(query,
                BlockTypeCondition.class,
                blockTypeCondition -> blockTypeCondition.getIdentifiers()[0],
                "minecraft:iron_block",
                ValueComparator.EQUALS);
        assertCondition(query, FlagCondition.class, flagCondition -> flagCondition.getFlags()[0], Flag.Build);
    }

    @Test
    void test7() {
        var query = WorldQuery.parse("deny flag=craft type=diamond_hoe group~=staff");
        assertEquals(QueryVerb.DENY, query.getVerb());
        assertEquals(3, query.getConditions().size());
        assertCondition(query, RegionNameCondition.class, regionNameCondition -> regionNameCondition.getNames()[0], "staff", ValueComparator.SIMILAR);
        assertCondition(query, RegionNameCondition.class, RegionNameCondition::isGroup, true);
    }

    static <T extends QueryCondition, R> void assertCondition(
            WorldQuery query,
            Class<T> conditionType,
            Function<T, ? extends R> actual,
            R expected
    ) {assertCondition(query, conditionType, actual, expected, null);}

    static <T extends QueryCondition, R> void assertCondition(
            WorldQuery query,
            Class<T> conditionType,
            Function<T, ? extends R> actual,
            R expected,
            @Nullable ValueComparator comparator
    ) {
        var condition   = query.getConditions().stream().flatMap(Streams.cast(conditionType)).findAny().orElseGet(Assertions::fail);
        var actualValue = actual.apply(condition);

        assertEquals(actualValue, expected);
        if (comparator != null && condition.getComparator() != null)
            assertEquals(comparator, condition.getComparator());
    }
}
