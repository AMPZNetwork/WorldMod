package com.ampznetwork.worldmod.core.query;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum ValueComparator implements BiPredicate<Object, Object> {
    EQUALS("=") {
        @Override
        public boolean test(Object l, Object r) {
            return Objects.equals(l, r);
        }

        @Override
        protected boolean test(long l, long r) {
            return l == r;
        }

        @Override
        protected boolean test(double l, double r) {
            return l == r;
        }
    }, NOT_EQUALS("!=") {
        @Override
        public boolean test(Object l, Object r) {
            return !EQUALS.test(l, r);
        }

        @Override
        protected boolean test(long l, long r) {
            return l != r;
        }

        @Override
        protected boolean test(double l, double r) {
            return l != r;
        }
    }, SIMILAR("~=") {
        @Override
        public boolean test(Object l, Object r) {
            if (l instanceof String str) return str.contains(String.valueOf(r));
            return tryNumberComparison(l, r) || EQUALS.test(l, r);
        }

        @Override
        protected boolean test(long l, long r) {
            return Math.max(l, r) - Math.min(l, r) < 16;
        }

        @Override
        protected boolean test(double l, double r) {
            return Math.max(l, r) - Math.min(l, r) < 16;
        }
    }, GREATER(">") {
        @Override
        public boolean test(Object l, Object r) {
            return tryNumberComparison(l, r) || EQUALS.test(l, r);
        }

        @Override
        protected boolean test(long l, long r) {
            return l > r;
        }

        @Override
        protected boolean test(double l, double r) {
            return l > r;
        }
    }, GREATER_EQUALS(">=") {
        @Override
        public boolean test(Object l, Object r) {
            return tryNumberComparison(l, r) || EQUALS.test(l, r);
        }

        @Override
        protected boolean test(long l, long r) {
            return l >= r;
        }

        @Override
        protected boolean test(double l, double r) {
            return l >= r;
        }
    }, LESSER("<") {
        @Override
        public boolean test(Object l, Object r) {
            return tryNumberComparison(l, r) || EQUALS.test(l, r);
        }

        @Override
        protected boolean test(long l, long r) {
            return l < r;
        }

        @Override
        protected boolean test(double l, double r) {
            return l < r;
        }
    }, LESSER_EQUALS("<=") {
        @Override
        public boolean test(Object l, Object r) {
            return tryNumberComparison(l, r) || EQUALS.test(l, r);
        }

        @Override
        protected boolean test(long l, long r) {
            return l <= r;
        }

        @Override
        protected boolean test(double l, double r) {
            return l <= r;
        }
    };

    public static ValueComparator find(String string) {
        return Arrays.stream(values())
                .filter(comp -> comp.string.equals(string))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("Cannot parse ValueComparator from string: " + string));
    }

    String string;

    @Override
    public abstract boolean test(Object base, Object key);

    @Override
    public String toString() {
        return string;
    }

    protected abstract boolean test(long l, long r);

    protected abstract boolean test(double l, double r);

    protected final boolean tryNumberComparison(Object l, Object r) {
        l = upN(l).orElse(l);
        r = upN(r).orElse(r);
        if (l instanceof Double ld && r instanceof Number) return test((double) ld, (double) r);
        if (l instanceof Long ll && r instanceof Number) return test((long) ll, (long) r);
        return false;
    }

    private Optional<Object> upN(Object x) {
        if (x instanceof Number) return Optional.ofNullable(i2l(x));
        var it = String.valueOf(x);
        if (it.matches("-?\\d+\\.\\d+")) return Optional.of(Double.parseDouble(it));
        if (it.matches("-?\\d+")) return Optional.of(Long.parseLong(it));
        return Optional.empty();
    }

    private Object i2l(Object x) {
        return x instanceof Integer i ? (long) i : x;
    }
}
