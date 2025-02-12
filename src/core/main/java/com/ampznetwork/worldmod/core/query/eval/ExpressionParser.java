package com.ampznetwork.worldmod.core.query.eval;

import com.ampznetwork.worldmod.core.query.eval.decl.Expression;
import com.ampznetwork.worldmod.core.query.eval.decl.Operator;
import com.ampznetwork.worldmod.core.query.eval.decl.OperatorExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.ParenthesesExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.val.NumberExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.val.VariableExpression;
import lombok.Value;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Arrays;

@Value
public class ExpressionParser extends StreamTokenizer {
    private static final int[] numParts = new int[]{ TT_NUMBER, '.' };
    private static final int[] varParts = new int[]{ TT_WORD, '.' };

    {
        ordinaryChar('.');
        ordinaryChar('+');
        ordinaryChar('-');
        ordinaryChar('*');
        ordinaryChar('/');
        ordinaryChar('%');
        ordinaryChar('(');
        ordinaryChar(')');
    }

    public ExpressionParser(Reader r) throws IOException {
        super(r);
        nextToken();
    }

    public Expression expr() throws IOException {
        var expr = switch (ttype) {
            case TT_WORD -> varExpr();
            case TT_NUMBER, '-' -> numExpr();
            case '(' -> parensExpr();
            default -> throw new IllegalStateException("Unexpected value: " + ttype);
        };
        // if next is an operator, return the operator expression instead
        for (var op : Operator.values())
            if (op.symbol == ttype)
                return opExpr(expr, op);
        nextToken();
        return expr;
    }

    public VariableExpression varExpr() throws IOException {
        var buf = new StringBuilder();
        do {
            buf.append(ttype == TT_NUMBER ? sval : String.valueOf(ttype));
            if (nextToken() == -1) break;
        } while (Arrays.binarySearch(varParts, ttype) >= 0);
        return new VariableExpression(buf.toString());
    }

    public NumberExpression<?> numExpr() throws IOException {
        var buf     = new StringBuilder();
        var decimal = false;
        do {
            buf.append(ttype == TT_NUMBER ? String.valueOf((int) nval) : String.valueOf(ttype));
            if (nextToken() == -1) break;
            if (!decimal) decimal = ttype == '.';
        } while (Arrays.binarySearch(numParts, ttype) >= 0);
        return decimal ? new NumberExpression<>(Double.parseDouble(buf.toString())) : new NumberExpression<Number>(Long.parseLong(buf.toString()));
    }

    public ParenthesesExpression parensExpr() throws IOException {
        nextToken();
        return new ParenthesesExpression(expr());
    }

    public OperatorExpression opExpr(Expression left, Operator op) throws IOException {
        // step to beginning of next expression
        nextToken();
        return new OperatorExpression(left, expr(), op);
    }
}
