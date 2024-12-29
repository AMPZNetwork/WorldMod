package com.ampznetwork.worldmod.core.event;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.delegate.Cancellable;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.WandType;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.mini.EventState;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.generated.PluginYml;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;
import org.comroid.api.func.util.Tuple;
import org.comroid.api.model.minecraft.model.DefaultPermissionValue;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.ampznetwork.worldmod.api.game.Flag.*;
import static net.kyori.adventure.text.Component.*;

@Log
@Value
@NonFinal
public abstract class EventDispatchBase {
    public static final int PER_PAGE = 8;
    Map<Player, Tuple.N2<Vector.N3, @NotNull Integer>> lookupRepeatCounter = new ConcurrentHashMap<>();
    WorldMod                                           mod;

    public EventState dependsOnFlag(Cancellable cancellable, Player player, Vector.N3 location, String worldName, Flag flagChain) {
        return dependsOnFlag(cancellable, player, location, worldName, Streams.OP.LogicalOr, Streams.OP.LogicalOr, flagChain);
    }

    public EventState dependsOnFlag(
            Cancellable adp,
            Object source,
            Vector.N3 location,
            String worldName,
            Streams.OP chainOp_cancel,
            Streams.OP chainOp_force,
            Flag flag
    ) {
        var     player = source instanceof Player p0 ? p0 : null;
        var     iter   = mod.findRegions(location, worldName).iterator();
        boolean cancel = false, force = false;
        while (iter.hasNext()) {
            var region   = iter.next();
            var usage    = region.getEffectiveFlagValueForPlayer(flag, player);
            var isGlobal = Region.GlobalRegionName.equals(region.getName());
            if (isGlobal && usage.getFlag().equals(Build) && usage.getState() != TriState.FALSE)
                continue; // exception for build flag on global region
            var state = usage.getState();
            if (state == TriState.NOT_SET) {
                if (player != null && Optional.ofNullable(region.getClaimOwner())
                        .map(DbObject::getId)
                        .filter(Predicate.not(player.getId()::equals))
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

    public boolean tryDispatchWandEvent(Cancellable cancellable, Player player, Vector.N3 location, WandType type, byte modifier) {
        if (cancellable.isCancelled()) return false;
        if (modifier == 0 || player == null || !mod.getLib().getPlayerAdapter()
                .checkPermission(player.getId(), type.usePermission.toString())
                .toBooleanOrElse(PluginYml.Permission.worldmod.lookup.wand.getDefault() == DefaultPermissionValue.TRUE)) {
            // not permitted
            // todo: send 'not permitted' message?
            return false;
        }
        var alt = (0x80 & modifier) == 0x80;
        modifier = (byte) (modifier & ~0x80);
        switch (type) {
            case selection:
                var selection = WorldModCommands.sel(player.getId());
                switch (modifier) {
                    case 1:
                        selection.x1((int) location.getX()).y1((int) location.getY()).z1((int) location.getZ());
                        break;
                    case 2:
                        selection.x2((int) location.getX()).y2((int) location.getY()).z2((int) location.getZ());
                        break;
                    default:
                        return false;
                }
                var tmp = selection.build();
                mod.getChat().target(player).sendMessage("Selection changed: ({},{},{}) -> ({},{},{})",
                        tmp.getX1(), tmp.getY1(), tmp.getZ1(), tmp.getX2(), tmp.getY2(), tmp.getZ2());
                break;
            case lookup:
                var txt = text();
                txt.append(mod.text().getLookupHeader(location)).append(text("\n"));
                var entries = mod.getLib().getEntityService().getAccessor(LogEntry.TYPE)
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
                        )).toList();
                var totalPages = (entries.size() / 8) + 1;
                var page = pollUseCounter(player, location, totalPages, alt);
                entries.stream()
                        .skip((long) (page - 1) * PER_PAGE)
                        .limit(PER_PAGE)
                        .map(mod.text()::ofLookupEntry)
                        .collect(Streams.atLeastOneOrElseGet(mod.text()::getEmptyListEntry))
                        .flatMap(each -> Stream.of(each, text("\n")))
                        .forEachOrdered(txt::append);
                txt.append(mod.text().getLookupFooter(page, totalPages));
                mod.getPlayerAdapter().send(player.getId(), txt.build());
                break;
        }
        cancellable.cancel();
        return true;
    }

    public void dispatchEvent(Cancellable cancellable, Object source, Object target, Vector.N3 location, String worldName, Flag flag) {
        if (cancellable.isCancelled()) return;
        if (passthrough(location, worldName))
            return;
        var player = source instanceof Player p0 ? p0 : null;
        var result = dependsOnFlag(cancellable, player, location, worldName, flag);
        if (result == EventState.Cancelled && player != null)
            mod.getLib().getPlayerAdapter().send(player.getId(),
                    text("You don't have permission to do that here").color(NamedTextColor.RED));
        mod.getLib().getScheduler().execute(() -> triggerLog(source, target, location, worldName, flag, result));
        log.finer(() -> "%s by %s at %s towards %s resulted in %s".formatted(cancellable, source, location, target, result));
    }

    private void triggerLog(Object source, Object target, Vector.N3 location, String worldName, Flag flag, EventState result) {
        if (mod.loggingSkipsNonPlayer() && !(source instanceof Player) && !(target instanceof Player))
            return;
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
        if (source instanceof Player player)
            builder.player(player);
        else builder.nonPlayerSource(source == null ? null : String.valueOf(source));
        if (target instanceof Player playerTarget)
            builder.target(playerTarget);
        else builder.nonPlayerTarget(target == null ? null : String.valueOf(target));
        mod.getEntityService().save(builder.build());
    }

    private int pollUseCounter(Player player, Vector.N3 location, int limit, boolean reverse) {
        return lookupRepeatCounter.compute(player, ($, pair) -> {
            if (pair == null || !pair.a.equals(location))
                return new Tuple.N2<>(location, 1);
            int n = pair.b;
            pair.b = reverse
                     ? (n > 1 ? n - 1 : limit)
                     : (n < limit ? n + 1 : 1);
            return pair;
        }).b;
    }
}
