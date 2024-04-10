package org.comroid.mcsd.api.model;

import org.comroid.api.func.util.Bitmask;
import org.jetbrains.annotations.Nullable;

public interface IStatusMessage {
    Status getStatus();
    Scope getScope();

    @Nullable
    default String getMessage() {
        return null;
    }

    default String toStatusMessage() {
        return getStatus().getEmoji() + '\t' + "Server is " + getStatus().getName();
    }

    enum Scope implements Bitmask.Attribute<Scope> {
        Public, Moderation
    }
}
