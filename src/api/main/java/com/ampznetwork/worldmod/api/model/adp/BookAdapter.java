package com.ampznetwork.worldmod.api.model.adp;

import net.kyori.adventure.text.Component;

import java.util.List;

public interface BookAdapter {
    String TITLE = "Interactive WorldMod Menu";
    String AUTHOR = "kaleidox@ampznetwork";

    List<Component[]> getPages();
}
