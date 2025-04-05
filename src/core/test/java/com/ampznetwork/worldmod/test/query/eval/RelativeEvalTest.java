package com.ampznetwork.worldmod.test.query.eval;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.interop.game.IPlayerAdapter;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.core.query.eval.decl.Expression;
import com.ampznetwork.worldmod.core.query.eval.model.QueryEvalContext;
import com.ampznetwork.worldmod.core.query.eval.model.RelativeTarget;
import org.comroid.api.data.Vector;
import org.comroid.api.info.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class RelativeEvalTest {
    QueryEvalContext context;

    @BeforeEach
    void setup() {
        var            id      = new UUID(0, 0);
        var            player  = Player.builder().id(id).name("dev").build();
        WorldMod       mod     = mock(WorldMod.class);
        IPlayerAdapter players = mock(IPlayerAdapter.class);

        expect(mod.getPlayerAdapter()).andReturn(players).anyTimes();
        expect(players.getPosition(id)).andReturn(new Vector.N3(5, 5, 5)).anyTimes();
        replay(mod, players);

        context = new QueryEvalContext(mod, player, RelativeTarget.POS_X, Map.of());
    }

    @Test
    public void basic() {
        assertEquals(15, ((Number) Expression.parse("~10").eval(context)).intValue());
    }

    @Test
    public void range() {
        var eval = Expression.parse("~10..-10").eval(context);

        assertInstanceOf(Range.class, eval);
        var range = (Range<?>) eval;

        assertNotNull(range.getStart());
        assertEquals(15, range.getStart().intValue());

        assertNotNull(range.getEnd());
        assertEquals(-5, range.getEnd().intValue());
    }
}
