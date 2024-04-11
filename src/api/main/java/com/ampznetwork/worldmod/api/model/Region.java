package com.ampznetwork.worldmod.api.model;

import com.ampznetwork.worldmod.api.model.mini.FlagContainer;
import com.ampznetwork.worldmod.api.model.mini.OwnedByParty;
import com.ampznetwork.worldmod.api.model.sel.Area;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

public interface Region extends Area, OwnedByParty, FlagContainer, Named {
    @Nullable Group getGroup();
}
