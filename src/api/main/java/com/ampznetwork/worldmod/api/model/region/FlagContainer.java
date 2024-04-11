package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.worldmod.api.game.Flag;
import net.kyori.adventure.util.TriState;

import java.util.stream.Stream;

public interface FlagContainer {
    Stream<Flag.Value> streamDeclaredFlags();

    default TriState getFlagState(Flag flag) {
        return streamDeclaredFlags()
                .filter(value -> value.flag().equals(flag))
                .map(Flag.Value::getState)
                .findFirst()
                .orElse(TriState.NOT_SET);
    }

    default Stream<Object> getFlagValues(Flag flag) {
        return streamDeclaredFlags()
                .filter(value -> value.flag().equals(flag))
                .map(Flag.Value::getValue);
    }
}
