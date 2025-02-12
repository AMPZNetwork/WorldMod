package com.ampznetwork.worldmod.test.query.eval;

import com.ampznetwork.worldmod.core.query.eval.decl.Expression;
import com.ampznetwork.worldmod.core.query.eval.decl.Operator;
import com.ampznetwork.worldmod.core.query.eval.decl.OperatorExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.val.NumberExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParseTest {
    @Test
    public void testSimple() {
        var expr = Expression.parse("1+2");
        Assertions.assertInstanceOf(OperatorExpression.class, expr);

        var op = (OperatorExpression) expr;
        Assertions.assertEquals(Operator.PLUS, op.getOp());
        Assertions.assertInstanceOf(NumberExpression.class, op.getLeft());
        Assertions.assertInstanceOf(NumberExpression.class, op.getRight());

        var l = (NumberExpression<?>) op.getLeft();
        Assertions.assertEquals(1L, l.getValue());

        var r = (NumberExpression<?>) op.getRight();
        Assertions.assertEquals(2L, r.getValue());
    }
}
