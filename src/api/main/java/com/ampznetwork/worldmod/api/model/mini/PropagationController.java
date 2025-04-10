package com.ampznetwork.worldmod.api.model.mini;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.model.region.FlagContainer;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Named;
import org.comroid.api.attr.UUIDContainer;

import java.util.Set;
import java.util.stream.Stream;

import static net.kyori.adventure.util.TriState.*;

public interface PropagationController extends UUIDContainer, OwnedByParty, FlagContainer, Named, Prioritized {
    Player getClaimOwner();

    Set<Player> getOwners();

    Set<Player> getMembers();

    default TriState getEffectiveFlagValueForPlayer(Flag flag, Player player) {
        return streamDeclaredFlags().filter(usage -> usage.getFlag().equals(flag)).filter(usage -> (switch (getRelation(player)) {
            case GUEST -> Flag.Usage.Target.Guests;
            case MEMBER, ENTITY -> Flag.Usage.Target.Members;
            case ADMIN -> Flag.Usage.Target.Owners;
        }).isFlagSet(usage.getTarget())).findAny().map(Flag.Usage::getState).orElse(NOT_SET);
    }

    @Override
    Stream<Flag.Usage> streamDeclaredFlags();
}