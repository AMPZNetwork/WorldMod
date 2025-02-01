package com.ampznetwork.worldmod.api.model.query;

import com.ampznetwork.worldmod.api.model.mini.EventState;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public enum QueryVerb implements UnaryOperator<EventState> {
    LOOKUP, DENY(EventState.Cancelled), ALLOW(EventState.Unaffected) {
        @Override
        public EventState apply(EventState current) {
            return current == EventState.Cancelled ? EventState.Unaffected : super.apply(current);
        }
    }, FORCE(EventState.Forced), PASSTHROUGH;
    protected final @Nullable EventState state;

    QueryVerb() {
        this(null);
    }

    QueryVerb(@Nullable EventState state) {
        this.state = state;
    }

    @Override
    public EventState apply(EventState current) {
        return state == null ? current : state;
    }
}
