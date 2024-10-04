package com.ampznetwork.worldmod.api.model.mini;

import com.ampznetwork.libmod.api.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface OwnedByParty {
    @Nullable Player getClaimOwner();

    Set<Player> getOwners();

    Set<Player> getMembers();

    default PlayerRelation getRelation(Player player) {
        if (player == null)
            return PlayerRelation.ENTITY;
        if (getOwners().contains(player) || player.equals(getClaimOwner()))
            return PlayerRelation.ADMIN;
        if (getMembers().contains(player))
            return PlayerRelation.MEMBER;
        return PlayerRelation.GUEST;
    }
}
