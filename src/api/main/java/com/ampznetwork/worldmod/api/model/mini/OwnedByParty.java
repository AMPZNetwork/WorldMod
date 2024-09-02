package com.ampznetwork.worldmod.api.model.mini;

import com.ampznetwork.libmod.api.entity.Player;

import java.util.Set;

public interface OwnedByParty {
    Set<Player> getOwners();

    Set<Player> getMembers();
}
