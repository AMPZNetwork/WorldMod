package com.ampznetwork.worldmod.test.query;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.core.query.condition.FlagCondition;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.BlockTypeCondition;
import com.ampznetwork.worldmod.core.query.condition.PositionCondition;
import com.ampznetwork.worldmod.core.query.condition.QueryCondition;
import com.ampznetwork.worldmod.core.query.condition.RadiusCondition;
import com.ampznetwork.worldmod.core.query.condition.RegionNameCondition;
import com.ampznetwork.worldmod.core.query.condition.SourceCondition;
import com.ampznetwork.worldmod.core.query.condition.WorldCondition;
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
        var query = WorldQuery.parse("lookup from=#fire radius=20");
        assertEquals(WorldQuery.Verb.LOOKUP, query.getVerb());
        assertEquals(2, query.getConditions().size());
        assertCondition(query, SourceCondition.class, SourceCondition::source, "#fire", WorldQuery.Comparator.EQUALS);
        assertCondition(query, RadiusCondition.class, RadiusCondition::radius, 20, WorldQuery.Comparator.EQUALS);
    }

    @Test
    void test2() {
        var query = WorldQuery.parse("lookup x=10..20 z=30..50 since=2w");
        assertEquals(WorldQuery.Verb.LOOKUP, query.getVerb());
        assertEquals(2, query.getConditions().size());
        assertCondition(query, PositionCondition.class, PositionCondition::getA, new Vector.N3(10, 0, 30), WorldQuery.Comparator.EQUALS);
        assertCondition(query, PositionCondition.class, PositionCondition::getB, new Vector.N3(20, 0, 50), WorldQuery.Comparator.EQUALS);
        //assertCondition(query, TimeCondition.class, TimeCondition::since, );
    }

    @Test
    void test3() {
        var query = WorldQuery.parse("lookup from=Kaleidox x=0 y=0 z=0 world=world type=minecraft:iron_block");
        assertEquals(WorldQuery.Verb.LOOKUP, query.getVerb());
        assertEquals(4, query.getConditions().size());
        assertCondition(query, SourceCondition.class, SourceCondition::source, "Kaleidox", WorldQuery.Comparator.EQUALS);
        assertCondition(query, WorldCondition.class, WorldCondition::worldName, "world", WorldQuery.Comparator.EQUALS);
        assertCondition(query, BlockTypeCondition.class, BlockTypeCondition::identifier, "minecraft:iron_block", WorldQuery.Comparator.EQUALS);
    }

    @Test
    void test4() {
        var query = WorldQuery.parse("allow tag~grave");
        assertEquals(WorldQuery.Verb.ALLOW, query.getVerb());
        assertEquals(1, query.getConditions().size());
        //assertCondition(query, );
    }

    @Test
    void test5() {
        var query = WorldQuery.parse("deny region=spawn");
        assertEquals(WorldQuery.Verb.DENY, query.getVerb());
        assertEquals(1, query.getConditions().size());
        assertCondition(query, RegionNameCondition.class, RegionNameCondition::name, "spawn", WorldQuery.Comparator.EQUALS);
        assertCondition(query, RegionNameCondition.class, RegionNameCondition::group, false);
    }

    @Test
    void test6() {
        var query = WorldQuery.parse("deny type=minecraft:iron_block flag=build");
        assertEquals(WorldQuery.Verb.DENY, query.getVerb());
        assertEquals(2, query.getConditions().size());
        assertCondition(query, BlockTypeCondition.class, BlockTypeCondition::identifier, "minecraft:iron_block", WorldQuery.Comparator.EQUALS);
        assertCondition(query, FlagCondition.class, FlagCondition::flag, Flag.Build);
    }

    @Test
    void test7() {
        var query = WorldQuery.parse("deny flag=craft type=diamond_hoe group~staff");
        assertEquals(WorldQuery.Verb.DENY, query.getVerb());
        assertEquals(3, query.getConditions().size());
        assertCondition(query, RegionNameCondition.class, RegionNameCondition::name, "staff", WorldQuery.Comparator.SIMILAR);
        assertCondition(query, RegionNameCondition.class, RegionNameCondition::group, true);
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
            @Nullable WorldQuery.Comparator comparator
    ) {
        var condition   = query.getConditions().stream().flatMap(Streams.cast(conditionType)).findAny().orElseGet(Assertions::fail);
        var actualValue = actual.apply(condition);

        assertEquals(actualValue, expected);
        if (comparator != null && condition.comparator() != null)
            assertEquals(comparator, condition.comparator());
    }
}
