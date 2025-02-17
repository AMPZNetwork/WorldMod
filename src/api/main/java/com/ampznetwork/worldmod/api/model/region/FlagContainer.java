package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.worldmod.api.flag.Flag;
import net.kyori.adventure.util.TriState;

import java.util.stream.Stream;

public interface FlagContainer {
    Stream<Flag.Usage> streamDeclaredFlags();

    default TriState getFlagState(Flag flag) {
        return streamDeclaredFlags()
                .filter(value -> value.getFlag().equals(flag))
                .map(Flag.Usage::getState)
                .findFirst()
                .orElse(TriState.NOT_SET);
    }

    default Stream<Flag.Usage> getFlagValues(Flag flag) {
        return streamDeclaredFlags()
                .filter(value -> value.getFlag().equals(flag));
    }
}
