package com.ampznetwork.worldmod.core.query.eval;

import com.ampznetwork.worldmod.core.query.ValueComparator;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractComparatorCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.SourceCondition;
import com.ampznetwork.worldmod.core.query.condition.impl.TargetCondition;
import com.ampznetwork.worldmod.core.query.eval.decl.Expression;
import com.ampznetwork.worldmod.core.query.eval.decl.val.VariableExpression;
import com.ampznetwork.worldmod.core.query.eval.model.QueryEvalContext;
import com.ampznetwork.worldmod.core.query.eval.model.VarSupplier;
import lombok.Value;
import org.comroid.api.func.util.Streams;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Value
public class ConditionalQueryEvaluator implements VarSupplier, Predicate<QueryEvalContext> {
    WorldQuery query;
    Expression expr, value;
    ValueComparator comparator;

    public ConditionalQueryEvaluator(WorldQuery query) {
        this.query = query;
        this.expr  = query.getConditions()
                .stream()
                .flatMap(Streams.cast(SourceCondition.class))
                .flatMap(src -> Arrays.stream(src.getSources()))
                .findAny()
                .map(Expression::parse)
                .orElseThrow();
        var target = query.getConditions().stream().flatMap(Streams.cast(TargetCondition.class)).findAny();
        this.value      = target.stream().flatMap(tgt -> Arrays.stream(tgt.getTargets())).findAny().map(Expression::parse).orElseThrow();
        this.comparator = target.map(AbstractComparatorCondition::getComparator).orElseThrow();
    }

    @Override
    public Stream<VariableExpression> vars() {
        return expr.append(value);
    }

    @Override
    public boolean test(QueryEvalContext context) {
        var result = expr.eval(context);
        var expect = value.eval(context);
        return comparator.test(result, expect);
    }

    @Override
    public String toString() {
        return "expr=" + expr + " value" + comparator + value;
    }
}
