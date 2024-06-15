package com.ampznetwork.worldmod.core.ui;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.adp.BookAdapter;
import lombok.Value;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

@Value
public class ClaimMenuBook implements BookAdapter {
    WorldMod worldMod;
    UUID playerId;

    @Override
    public List<Component[]> getPages() {
        return List.of(toc(), details(), members(), flags());
    }

    private Component[] toc() {
        return new Component[]{text("table of contents (wip)")};
    }

    private Component[] details() {
        return new Component[]{text("details (wip)")};
    }

    private Component[] members() {
        return new Component[]{text("members (wip)")};
    }

    private Component[] flags() {
        return new Component[]{text("flags (wip)")};
    }
}
