package com.ampznetwork.worldmod.core.event;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.delegate.Cancellable;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.WandType;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.mini.EventState;
import com.ampznetwork.worldmod.api.model.query.IWorldQuery;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.api.model.query.QueryVerb;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.generated.PluginYml;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;
import org.comroid.api.func.util.Tuple;
import org.comroid.api.model.minecraft.model.DefaultPermissionValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Stream;

import static com.ampznetwork.worldmod.api.game.Flag.*;
import static net.kyori.adventure.text.Component.*;
import static org.comroid.api.func.util.Streams.*;

@Log
@Value
@NonFinal
public abstract class EventDispatchBase {
    public static final int PER_PAGE = 8;
    Map<Player, Tuple.N2<Vector.N3, @NotNull Integer>> lookupRepeatCounter = new ConcurrentHashMap<>();
    WorldMod                                           mod;

    public EventState dependsOnFlag(Player player, Vector.N3 location, String worldName, Flag flagChain) {
        return dependsOnFlag(player, location, worldName, OP.LogicalOr, OP.LogicalOr, flagChain);
    }

    public EventState dependsOnFlag(Object source, Vector.N3 location, String worldName, OP chainOp_cancel, OP chainOp_force, Flag flag) {
        var     player = source instanceof Player p0 ? p0 : null;
        var     iter   = mod.findRegions(location, worldName).iterator();
        boolean cancel = false, force = false;
        while (iter.hasNext()) {
            var region   = iter.next();
            var usage    = region.getEffectiveFlagValueForPlayer(flag, player);
            var isGlobal = Region.GLOBAL_REGION_NAME.equals(region.getName());
            if (isGlobal && usage.getFlag().equals(Build) && usage.getState() != TriState.FALSE) continue; // exception for build flag on global region
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
            if (state == TriState.FALSE) cancel = chainOp_cancel.test(cancel, true);
            else if (state == TriState.TRUE && usage.isForce()) force = chainOp_force.test(force, true);
        }
        if (force) return EventState.Forced;
        else if (cancel) return EventState.Cancelled;
        return EventState.Unaffected;
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
        if (modifier == 0 || player == null || !mod.getLib()
                .getPlayerAdapter()
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
                mod.chat()
                        .target(player)
                        .sendMessage("Selection changed: ({},{},{}) -> ({},{},{})",
                                tmp.getX1(),
                                tmp.getY1(),
                                tmp.getZ1(),
                                tmp.getX2(),
                                tmp.getY2(),
                                tmp.getZ2());
                break;
            case lookup:
                var txt = text();
                txt.append(mod.text().getLookupHeader(location)).append(text("\n"));
                var entries = mod.getLib()
                        .getEntityService()
                        .getAccessor(LogEntry.TYPE)
                        .querySelect("""
                                        select * from worldmod_world_log log
                                            where timestamp > :timestamp
                                            and ( x = :x and y = :y and z = :z )
                                            order by timestamp desc
                                        """,
                                Map.of("timestamp", Instant.now().minus(Duration.ofDays(3)), "x", location.getX(), "y", location.getY(), "z", location.getZ()))
                        .toList();
                var totalPages = (entries.size() / 8) + 1;
                var page = pollUseCounter(player, location, totalPages, alt);
                entries.stream()
                        .skip((long) (page - 1) * PER_PAGE)
                        .limit(PER_PAGE)
                        .map(mod.text()::ofLookupEntry)
                        .collect(atLeastOneOrElseGet(mod.text()::getEmptyListEntry))
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
        log.fine("Dispatching %s: [%s] %s [%s] @ %s in %s".formatted(cancellable.getClass().getSimpleName(),
                source,
                flag.getCanonicalName(),
                target,
                location,
                worldName));
        final EventState[] result = new EventState[]{ EventState.Unaffected };
        var                player = source instanceof Player p0 ? p0 : null;

        try {
            //if (cancellable.isCancelled()) return;
            if (passthrough(location, worldName)) return;

            final var queryVars = mod.flagInvokeCount(player);

            var managers = mod.getQueryManagers();
            var queries = Stream.concat(Stream.ofNullable(managers.getOrDefault(worldName, null)).flatMap(mgr -> mgr.getQueries().stream()),
                    managers.get(Region.GLOBAL_REGION_NAME).getQueries().stream()).toList();
            final var data = qidBuilder(player, source, target, location, worldName, flag).timestamp(Instant.now()).build();

            end:
            {
                // check for passthrough queries
                if (queries.stream().filter(proxy(IWorldQuery::getVerb, QueryVerb.PASSTHROUGH::equals)).anyMatch(query -> query.test(mod, data)))
                    // do not handle event as it is set to pass through
                    break end;

                // run flag checks
                result[0] = dependsOnFlag(player, location, worldName, flag);

                // check for force queries
                if (queries.stream().filter(proxy(IWorldQuery::getVerb, QueryVerb.FORCE::equals)).anyMatch(query -> query.test(mod, data))) {
                    result[0] = EventState.Forced;
                    break end;
                }

                // evaluate allow/deny and conditional queries when unaffected
                if (result[0] == EventState.Unaffected) {
                    queries.stream()
                            .filter(proxy(IWorldQuery::getVerb, Set.of(QueryVerb.ALLOW, QueryVerb.DENY)::contains))
                            .filter(query -> query.test(mod, data))
                            .forEach(query -> {
                                if (query.getMessageKey() != null && player != null) query.getMessage(mod)
                                        .ifPresent(msg -> mod.getPlayerAdapter().send(player.getId(), msg));
                                result[0] = query.getVerb().apply(result[0]);
                            });
                    if (queries.stream()
                            .filter(proxy(IWorldQuery::getVerb, QueryVerb.CONDITIONAL::equals))
                            .filter(query -> query.test(mod, data))
                            .flatMap(Streams.cast(WorldQuery.class))
                            .flatMap(query -> Stream.ofNullable(query.getEvaluator()))
                            .anyMatch(eval -> !eval.test(queryVars))) result[0] = EventState.Cancelled;
                }
            }
        } catch (Throwable t) {
            if (result[0] == EventState.Unaffected && mod.isSafeMode() && (player == null || !mod.getPlayerAdapter()
                    .checkOpLevel(player.getId(), 1))) result[0] = EventState.Cancelled;

            log.log(Level.SEVERE, "Could not handle " + cancellable + "; falling back to " + result[0].name(), t);
        } finally {
            if (result[0] == EventState.Cancelled && player != null) mod.getLib()
                    .getPlayerAdapter()
                    .send(player.getId(), text("You don't have permission to do that here").color(NamedTextColor.RED));

            // apply state
            if (result[0] == EventState.Cancelled && !cancellable.isCancelled()) cancellable.cancel();
            else if (result[0] == EventState.Forced && !cancellable.isForced()) cancellable.force();

            log.finer(() -> "%s by %s at %s towards %s resulted in %s".formatted(cancellable, source, location, target, result[0]));
            triggerLog(source, target, location, worldName, flag, result[0]);
        }
    }

    private QueryInputData.Builder qidBuilder(@Nullable Player player, Object source, Object target, Vector.N3 location, String worldName, Flag flag) {
        //noinspection PatternValidation
        return QueryInputData.builder()
                .serverName(mod.getLib().getServerName())
                .worldName(worldName)
                .player(player)
                .action(flag)
                .position(location)
                .nonPlayerSource(String.valueOf(source))
                .targetResourceKey(Key.key(String.valueOf(target).toLowerCase().replaceAll("[-.]", "_")));
    }

    private void triggerLog(Object source, Object target, Vector.N3 location, String worldName, Flag flag, EventState result) {
        if (mod.loggingSkipsNonPlayer() && !(source instanceof Player) && !(target instanceof Player)) return;
        final var fNames = flag.getCanonicalName().split("\\.");
        if (mod.loggingSkipFlagNames().map(name -> name.split("\\.")).anyMatch(names -> {
            for (var i = 0; i < fNames.length && i < names.length; i++) {
                if (!fNames[i].equals(names[i])) return false; // doesnt match any more
                if ("*".equals(names[i])) break; // wildcard
            }
            return true; // all parts matched up to this point
        })) return;
        var builder = LogEntry.builder()
                .serverName(mod.getLib().getServerName())
                .worldName(worldName)
                .action(flag.getCanonicalName())
                .x((int) location.getX())
                .y((int) location.getY())
                .z((int) location.getZ())
                .result(result);
        if (source instanceof Player player) builder.player(player);
        else builder.nonPlayerSource(source == null ? null : String.valueOf(source));
        if (target instanceof Player playerTarget) builder.target(playerTarget);
        else builder.nonPlayerTarget(target == null ? null : String.valueOf(target));
        mod.getEntityService().save(builder.build());
    }

    private int pollUseCounter(Player player, Vector.N3 location, int limit, boolean reverse) {
        return lookupRepeatCounter.compute(player, ($, pair) -> {
            if (pair == null || !pair.a.equals(location)) return new Tuple.N2<>(location, 1);
            int n = pair.b;
            pair.b = reverse ? (n > 1 ? n - 1 : limit) : (n < limit ? n + 1 : 1);
            return pair;
        }).b;
    }
}
