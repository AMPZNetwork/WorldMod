package com.ampznetwork.worldmod.api.model.mini;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.region.FlagContainer;
import com.ampznetwork.worldmod.api.model.region.Region;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.ampznetwork.worldmod.api.model.mini.PlayerRelation.*;
import static net.kyori.adventure.util.TriState.*;

public interface PropagationController extends OwnedByParty, FlagContainer, Named {
    default Flag.Usage getEffectiveFlagValueForPlayer(Flag flag, UUID playerId) {
        if (playerId == null)
            return Flag.Usage.builder()
                    .flag(flag)
                    .build();
        if (this instanceof Region rg && playerId.equals(rg.getClaimOwner()))
            return Flag.Usage.builder()
                    .flag(flag)
                    .state(TRUE)
                    .force(true)
                    .priority(999)
                    .build();
        var values = getFlagValues(flag).toList();
        var builder = Flag.Usage.builder().flag(flag).state(NOT_SET);
        if (values.isEmpty())
            return builder.build();
        var me = getOwnerIDs().contains(playerId) ? ADMIN
                                                  : getMemberIDs().contains(playerId) ? MEMBER
                                                                                      : GUEST;
        var explicit = new Flag.Usage[4]; // indices: see PlayerRelation#ordinal()
        var mask = 0;

        // scan all values for explicit states
        for (var value : values)
            for (var selector : value.getSelectors()) {
                var tgt = find(selector.getBase(), selector.getType());
                if ((explicit[tgt.ordinal()] = value).getState() != NOT_SET)
                    mask |= (int) tgt.getAsLong();
            }

        // decide result state based upon explicit values
        @Nullable TriState choice = null;
        // fall back to own if possible
        var mine = explicit[me.ordinal()];
        if (mine != null)
            return mine;
            // if only owner is set; everyone below should get false
        else if (mask == ADMIN.getAsLong())
            choice = me.ordinal() <= MEMBER.ordinal() ? FALSE : TRUE;
            // if only member is set; everyone below should get false; above should get true
        else if (mask == MEMBER.getAsLong())
            choice = me.ordinal() == GUEST.ordinal() ? FALSE : TRUE;

        // if there is no result yet and guest is set to false; everyone above should get true
        var guest = explicit[GUEST.ordinal()];
        if (choice == null
            && guest != null
            && (choice = guest.getState()) != null
            && choice == FALSE // choice used as a buffer
            && me.ordinal() > GUEST.ordinal())
            choice = TRUE;
            // otherwise or if entity is set; return global decision
        else choice = Region.GlobalRegionName.equals(getName()) ? NOT_SET : FALSE;
        return builder.state(choice).build();
    }
}
