package com.ampznetwork.worldmod.core.query.eval.decl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public enum Operator {
    PLUS('+') {
        @Override
        public long apply(long a, long b) {
            return a + b;
        }

        @Override
        public double apply(double a, double b) {
            return a + b;
        }
    }, MINUS('-') {
        @Override
        public long apply(long a, long b) {
            return a - b;
        }

        @Override
        public double apply(double a, double b) {
            return a - b;
        }
    }, MULTIPLY('*') {
        @Override
        public long apply(long a, long b) {
            return a * b;
        }

        @Override
        public double apply(double a, double b) {
            return a * b;
        }
    }, DIVIDE('/') {
        @Override
        public long apply(long a, long b) {
            return a / b;
        }

        @Override
        public double apply(double a, double b) {
            return a / b;
        }
    }, MODULUS('%') {
        @Override
        public long apply(long a, long b) {
            return a % b;
        }

        @Override
        public double apply(double a, double b) {
            return a % b;
        }
    };
    char symbol;

    public abstract long apply(long a, long b);

    public abstract double apply(double a, double b);

    public Number apply(Number a, Number b) {
        if (a instanceof Double ad)
            return apply(ad.doubleValue(), (double) b);
        return apply((long) a, (long) b);
    }
}
