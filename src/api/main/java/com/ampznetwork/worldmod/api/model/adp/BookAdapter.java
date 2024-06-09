package com.ampznetwork.worldmod.api.model.adp;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BookAdapter {
    @NotNull
    List<String> getPages();

    int getPageCount();

    @NotNull
    String getDisplayName();
}
