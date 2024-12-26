package com.ampznetwork.worldmod.api.model.mini;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Named;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EventState implements Named {
    Unaffected(TriState.NOT_SET),
    Cancelled(TriState.FALSE),
    Forced(TriState.TRUE);
    TriState equivalent;
}
