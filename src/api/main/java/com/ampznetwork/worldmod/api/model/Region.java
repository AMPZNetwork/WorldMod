package com.ampznetwork.worldmod.api.model;

import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

public interface Region extends Area, OwnedByParty, FlagContainer, Named {
    @Nullable Group getGroup();
}