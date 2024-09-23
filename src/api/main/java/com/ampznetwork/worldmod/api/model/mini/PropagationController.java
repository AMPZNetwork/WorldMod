package com.ampznetwork.worldmod.api.model.mini;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.region.FlagContainer;
import com.ampznetwork.worldmod.api.model.region.Region;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static com.ampznetwork.worldmod.api.model.mini.PlayerRelation.*;
import static net.kyori.adventure.util.TriState.*;

public interface PropagationController extends OwnedByParty, FlagContainer, Named {
    default Flag.Usage getEffectiveFlagValueForPlayer(Flag flag, Player player) {
        return streamDeclaredFlags()
                .filter(usage -> usage.getFlag().equals(flag))
                .filter(usage -> {
                    // evaluate selectors
                    var relation = getRelation(player);
                    // todo: evaluate position, radius etc selector attributes
                    return usage.getSelectors().stream().anyMatch(selector -> selector.getBase().equals(relation.getSelector()));
                })
                .findAny().orElseGet(() -> Flag.Usage.builder().flag(flag).build());
    }
}
