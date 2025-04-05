package com.ampznetwork.worldmod.test.query.eval;

import com.ampznetwork.worldmod.core.query.eval.decl.Expression;
import com.ampznetwork.worldmod.core.query.eval.decl.Operator;
import com.ampznetwork.worldmod.core.query.eval.decl.OperatorExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.RelativeExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.val.NumberExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.val.RangeExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.val.VariableExpression;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParseTest {
    @Test
    public void testTwoNumbers() {
        var expr = Expression.parse("1+2");
        assertInstanceOf(OperatorExpression.class, expr);

        var op = (OperatorExpression) expr;
        assertEquals(Operator.PLUS, op.getOp());
        assertInstanceOf(NumberExpression.class, op.getLeft());
        assertInstanceOf(NumberExpression.class, op.getRight());

        var l = (NumberExpression<?>) op.getLeft();
        assertEquals(1L, l.getValue());

        var r = (NumberExpression<?>) op.getRight();
        assertEquals(2L, r.getValue());
    }

    @Test
    public void testRange() {
        var expr = Expression.parse("1..2");
        assertInstanceOf(RangeExpression.class, expr);

        var range = (RangeExpression<?>) expr;
        assertInstanceOf(NumberExpression.class, range.getLow());
        assertInstanceOf(NumberExpression.class, range.getHigh());

        var l = (NumberExpression<?>) range.getLow();
        assertEquals(1L, l.getValue());

        var r = (NumberExpression<?>) range.getHigh();
        assertEquals(2L, r.getValue());
    }

    @Test
    public void testTwoVariables() {
        var expr = Expression.parse("build.place-build.break");
        assertInstanceOf(OperatorExpression.class, expr);

        var op = (OperatorExpression) expr;
        assertEquals(Operator.MINUS, op.getOp());
        assertInstanceOf(VariableExpression.class, op.getLeft());
        assertInstanceOf(VariableExpression.class, op.getRight());

        var l = (VariableExpression) op.getLeft();
        assertEquals("build.place", l.getKey());

        var r = (VariableExpression) op.getRight();
        assertEquals("build.break", r.getKey());
    }

    @Test
    public void testMixed() {
        var expr = Expression.parse("build.place/3");
        assertInstanceOf(OperatorExpression.class, expr);

        var op = (OperatorExpression) expr;
        assertEquals(Operator.DIVIDE, op.getOp());
        assertInstanceOf(VariableExpression.class, op.getLeft());
        assertInstanceOf(NumberExpression.class, op.getRight());

        var l = (VariableExpression) op.getLeft();
        assertEquals("build.place", l.getKey());

        var r = (NumberExpression<?>) op.getRight();
        assertEquals(3L, r.getValue());
    }

    @Nested
    @SuppressWarnings("unchecked")
    class Relative {
        @Test
        public void basic() {
            var expr = Expression.parse("~10");
            assertInstanceOf(RelativeExpression.class, expr);

            var rel = (RelativeExpression) expr;
            assertInstanceOf(NumberExpression.class, rel.getDelta());

            var x = (NumberExpression<Integer>) rel.getDelta();
            assertEquals(10, x.getValue());
        }

        @Test
        public void range() {
            var expr = Expression.parse("~10..-10");
            assertInstanceOf(RelativeExpression.class, expr);

            var rel = (RelativeExpression) expr;
            assertInstanceOf(RangeExpression.class, rel.getDelta());

            var range = (RangeExpression<Integer>) rel.getDelta();
            assertNotNull(range.getLow());
            assertEquals(10, range.getLow().getValue());
            assertNotNull(range.getHigh());
            assertEquals(-10, range.getHigh().getValue());
        }
    }
}
