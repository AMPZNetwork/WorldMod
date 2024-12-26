package com.ampznetwork.worldmod.core.event;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.util.chat.BroadcastType;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.WandType;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.mini.EventState;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.ampznetwork.worldmod.api.game.Flag.*;

@Log
@Value
@NonFinal
public abstract class EventDispatchBase {
    WorldMod mod;

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
        var player = mod.getPlayerAdapter().convertNativePlayer(source).orElse(null);
        var     playerId = player == null ? null : player.getId();
        var     iter     = mod.findRegions(location, worldName).iterator();
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
        return mod.findRegions(location, worldName)
                .map(region -> region.getFlagState(Passthrough))
                .findFirst()
                .filter(state -> state == TriState.TRUE)
                .isPresent();
    }

    public boolean tryDispatchWandEvent(IPropagationAdapter cancellable, String worldName, Player player, Vector.N3 location, WandType type, byte modifier) {
        if (player == null || mod.getLib().getPlayerAdapter().checkPermission(player.getId(), type.usePermission) != TriState.TRUE) {
            // not permitted
            // todo: send 'not permitted' message?
            return false;
        }
        switch (type) {
            case selection:
                var selection = WorldModCommands.sel(player.getId());
                switch (modifier) {
                    case 0:
                        selection.x1((int) location.getX()).y1((int) location.getY()).z1((int) location.getZ());
                        break;
                    case 1:
                        selection.x2((int) location.getX()).y2((int) location.getY()).z2((int) location.getZ());
                        break;
                    default:
                        return false;
                }
                var tmp=selection.build();
                mod.getChat().target(player).sendMessage("Selection changed: ({},{},{}) -> ({},{},{})",
                        tmp.getX1(),tmp.getY1(),tmp.getZ1(), tmp.getX2(),tmp.getY2(),tmp.getZ2());
                break;
            case lookup:
                mod.getLib().getEntityService().getAccessor(LogEntry.TYPE)
                        .querySelect("""
                                select * from worldmod_world_log log
                                    where timestamp > :timestamp
                                    and ( x = :x and y = :y and z = :z )
                                    order by timestamp desc
                                """, Map.of(
                                        "timestamp", Instant.now().minus(Duration.ofDays(3)),
                                        "x", location.getX(),
                                        "y", location.getY(),
                                        "z", location.getZ()
                                ))
                        .limit(8)
                        .forEachOrdered(log -> mod.getChat().target(player).sendMessage(BroadcastType.HINT,
                                "[{}] {} was {} by {} ({})",
                                DateTimeFormatter.ofPattern("dd.MM.yyyy mm:HH"),
                                Optional.ofNullable(log.getTarget()).map(Player::getName).orElseGet(log::getNonPlayerTarget),
                                log.getAction(),
                                Optional.ofNullable(log.getPlayer()).map(Player::getName).orElseGet(log::getNonPlayerSource),
                                log.getResult()));
                break;
        }
        cancellable.cancel();
        return true;
    }

    public void dispatchEvent(IPropagationAdapter cancellable, Object source, Object target, Vector.N3 location, String worldName, Flag flag) {
        if (passthrough(location, worldName))
            return;
        Player playerSource = mod.getPlayerAdapter().convertNativePlayer(source).orElse(null);
        var    result       = dependsOnFlag(cancellable, playerSource, location, worldName, flag);
        if (result == EventState.Cancelled && playerSource != null)
            mod.getLib().getPlayerAdapter().send(playerSource.getId(),
                    Component.text("You don't have permission to do that here").color(NamedTextColor.RED));
        mod.getLib().getScheduler().execute(() -> triggerLog(source, target, location, worldName, flag, result));
        log.finer(() -> "%s by %s at %s towards %s resulted in %s".formatted(cancellable, source, location, target, result));
    }

    private void triggerLog(Object source, Object target, Vector.N3 location, String worldName, Flag flag, EventState result) {
        final var fNames = flag.getCanonicalName().split("\\.");
        if (mod.loggingSkipFlagNames()
                .map(name -> name.split("\\."))
                .anyMatch(names -> {
                    for (var i = 0; i < fNames.length && i < names.length; i++) {
                        if (!fNames[i].equals(names[i]))
                            return false; // doesnt match any more
                        if ("*".equals(names[i]))
                            break; // wildcard
                    }
                    return true; // all parts matched up to this point
                }))
            return;
        var builder = LogEntry.builder()
                .worldName(worldName)
                .action(flag.getCanonicalName())
                .x((int) location.getX())
                .y((int) location.getY())
                .z((int) location.getZ())
                .result(result);
        Player playerSource = mod.getPlayerAdapter().convertNativePlayer(source).orElse(null);
        if (playerSource != null)
            builder.player(playerSource);
        else builder.nonPlayerSource(String.valueOf(source));
        Player playerTarget = mod.getPlayerAdapter().convertNativePlayer(target).orElse(null);
        if (playerTarget != null)
            builder.target(playerTarget);
        else builder.nonPlayerTarget(String.valueOf(target));
        if (mod.loggingSkipsNonPlayer() && playerSource == null && playerTarget == null)
            return;
        mod.getEntityService().save(builder.build());
    }
}
