package com.ampznetwork.worldmod.core.event;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.mini.EventState;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static com.ampznetwork.worldmod.api.game.Flag.*;

@Log
@Value
@NonFinal
public class EventDispatchBase {
    WorldMod worldMod;

    public EventState dependsOnFlag(IPropagationAdapter cancellable, Player player, Vector.N3 location, String worldName, Flag flagChain) {
        return dependsOnFlag(cancellable, player, location, worldName, Streams.OP.LogicalOr, Streams.OP.LogicalOr, flagChain);
    }

    public EventState dependsOnFlag(
            IPropagationAdapter adp,
            Object source,
            Vector.N3 location,
            String worldName,
            Streams.OP chainOp_cancel,
            Streams.OP chainOp_force,
            Flag flag
    ) {
        var     player   = tryGetAsPlayer(source);
        var     playerId = player == null ? null : player.getId();
        var     iter     = worldMod.findRegions(location, worldName).iterator();
        boolean cancel   = false, force = false;
        while (iter.hasNext()) {
            var region   = iter.next();
            var usage    = region.getEffectiveFlagValueForPlayer(flag, player);
            var isGlobal = Region.GlobalRegionName.equals(region.getName());
            if (isGlobal && usage.getFlag().equals(Build) && usage.getState() != TriState.FALSE)
                continue; // exception for build flag on global region
            var state = usage.getState();
            if (state == TriState.NOT_SET) {
                if (playerId != null && Optional.ofNullable(region.getClaimOwner())
                        .map(DbObject::getId)
                        .filter(Predicate.not(playerId::equals))
                        .isPresent())
                    //noinspection DataFlowIssue
                    cancel = chainOp_cancel.test(cancel, !(boolean) usage.getFlag().getDefaultValue());
                else continue;
            }
            if (state == TriState.FALSE)
                cancel = chainOp_cancel.test(cancel, true);
            else if (state == TriState.TRUE && usage.isForce())
                force = chainOp_force.test(force, true);
        }
        if (force) {
            adp.force();
            return EventState.Forced;
        } else if (cancel) {
            adp.cancel();
            return EventState.Cancelled;
        } else return EventState.Unaffected;
    }

    public boolean passthrough(Vector.N3 location, String worldName) {
        return worldMod.findRegions(location, worldName)
                .map(region -> region.getFlagState(Passthrough))
                .findFirst()
                .filter(state -> state == TriState.TRUE)
                .isPresent();
    }

    private @Nullable Player tryGetAsPlayer(Object it) {
        return tryGetAsPlayer(worldMod,it);
    }
    public static @Nullable Object tryGetAsPlayer(WorldMod mod, Object it, Object or) {
        var r = tryGetAsPlayer(mod, it);
        return r != null ? r : or;
    }
    public static @Nullable Player tryGetAsPlayer(WorldMod mod, Object it) {
        return it == null ? null : switch (it) {
            case Player plr -> plr;
            case UUID playerId -> mod.getEntityService().getAccessor(Player.TYPE).getOrCreate(playerId).orElseThrow();
            default -> null;
        };
    }

    public void dispatchEvent(IPropagationAdapter cancellable, Object source, Object target, Vector.N3 location, String worldName, Flag flag) {
        if (passthrough(location, worldName))
            return;
        Player playerSource = tryGetAsPlayer(source);
        var    result       = dependsOnFlag(cancellable, playerSource, location, worldName, flag);
        if (result == EventState.Cancelled && playerSource != null)
            worldMod.getLib().getPlayerAdapter().send(playerSource.getId(),
                    Component.text("You don't have permission to do that here").color(NamedTextColor.RED));
        worldMod.getLib().getScheduler().execute(() -> triggerLog(source, target, location, worldName, flag, result));
        log.finer(() -> "%s by %s at %s towards %s resulted in %s".formatted(cancellable, source, location, target, result));
    }

    private void triggerLog(Object source, Object target, Vector.N3 location, String worldName, Flag flag, EventState result) {
        var builder = LogEntry.builder()
                .worldName(worldName)
                .action(flag.getCanonicalName())
                .position(location)
                .result(result);
        Player playerSource = tryGetAsPlayer(source);
        if (playerSource != null)
            builder.player(playerSource);
        else builder.nonPlayerSource(String.valueOf(source));
        Player playerTarget = tryGetAsPlayer(target);
        if (playerTarget != null)
            builder.target(playerTarget);
        else builder.nonPlayerTarget(String.valueOf(target));
        worldMod.getEntityService().save(builder.build());
    }
}
