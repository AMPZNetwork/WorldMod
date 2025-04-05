package com.ampznetwork.worldmod.core.query.eval.model;

import org.comroid.api.data.Vector;

public enum RelativeTarget {
    POS_X {
        @Override
        public Number get(Vector.N3 offset) {
            return offset.getX();
        }
    }, POS_Y {
        @Override
        public Number get(Vector.N3 offset) {
            return offset.getY();
        }
    }, POS_Z {
        @Override
        public Number get(Vector.N3 offset) {
            return offset.getZ();
        }
    };

    public abstract Number get(Vector.N3 offset);
}
