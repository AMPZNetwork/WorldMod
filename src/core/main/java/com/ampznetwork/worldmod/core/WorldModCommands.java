package com.ampznetwork.worldmod.core;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.AutoFillProvider;
import com.ampznetwork.libmod.api.util.NameGenerator;
import com.ampznetwork.libmod.api.util.Util;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.mini.PlayerRelation;
import com.ampznetwork.worldmod.api.model.query.IQueryManager;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.ui.ClaimMenuBook;
import com.ampznetwork.worldmod.generated.PluginYml.Permission.worldmod;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;
import org.comroid.annotations.Alias;
import org.comroid.annotations.Default;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;
import org.comroid.api.text.StringMode;
import org.comroid.api.text.Word;
import org.comroid.api.text.minecraft.McFormatCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.ampznetwork.worldmod.api.WorldMod.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.*;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@UtilityClass
public class WorldModCommands {
    private static final Map<UUID, Area.Builder> selections      = new ConcurrentHashMap<>();
    public static final int MAX_LINE_LENGTH = 45;

    @Alias("sel")
    @Command(permission = worldmod.SELECTION, privacy = Command.PrivacyLevel.PRIVATE)
    public String select(UUID playerId, @Command.Arg @Nullable Shape type) {
        if (type == null) {
            selections.remove(playerId);
            return "Selection cleared";
        }
        sel(playerId).shape(type);
        clearSel(playerId);
        return "Now selecting as " + type.name();
    }

    @Command
    public Component lookup(WorldMod mod, Player player, @Command.Arg(stringMode = StringMode.GREEDY) String query) {
        var parse = WorldQuery.parse(query);
        return mod.getEntityService()
                .getAccessor(LogEntry.TYPE)
                .querySelect(parse.toLookupQuery(mod))
                .collect(Collector.of(Component::text,
                        (txt, log) -> txt.append(text("\n").append(text(log.getAction()))),
                        ComponentBuilder::append,
                        ComponentBuilder::build));
    }

    public static Area.Builder sel(UUID playerId) {
        return selections.computeIfAbsent(playerId, $ -> Area.builder());
    }

    public static void clearSel(UUID playerId) {
        sel(playerId).x1(null)
                .y1(null)
                .z1(null)
                .x2(null)
                .y2(null)
                .z2(null)
                .x3(null)
                .y3(null)
                .z3(null)
                .x4(null)
                .y4(null)
                .z4(null)
                .x5(null)
                .y5(null)
                .z5(null)
                .x6(null)
                .y6(null)
                .z6(null)
                .x7(null)
                .y7(null)
                .z7(null)
                .x8(null)
                .y8(null)
                .z8(null);
    }

    @Alias("pos")
    @Command(permission = worldmod.SELECTION, privacy = Command.PrivacyLevel.PRIVATE)
    public static class position {
        @Command
        public static String $(WorldMod worldMod, UUID playerId, @Command.Arg(autoFill = { "1", "2" }) int index) {
            var pos = worldMod.getLib().getPlayerAdapter().getPosition(playerId);
            if (index == 1) sel(playerId).x1((int) pos.getX()).y1((int) pos.getY()).z1((int) pos.getZ());
            else if (index == 2) sel(playerId).x2((int) pos.getX()).y2((int) pos.getY()).z2((int) pos.getZ());
            return "Set position " + index;
        }

        @Command
        public static String clear(UUID playerId) {
            clearSel(playerId);
            return "Selection cleared";
        }
    }

    @Command
    @Alias("region")
    public static class claim {
        @Command(permission = worldmod.CLAIM, privacy = Command.PrivacyLevel.PRIVATE)
        public static String $(WorldMod worldMod, UUID playerId, @Nullable @Command.Arg(required = false) String name) {
            var player = worldMod.getLib().getPlayerAdapter().getPlayer(playerId).orElseThrow();
            if (!selections.containsKey(playerId)) throw new Command.Error("No area selected!");
            var sel = sel(playerId).build();
            sel.validateShapeMask();
            var world = worldMod.getLib().getPlayerAdapter().getWorldName(playerId);
            var rg = Region.builder().serverName(worldMod.getLib().getServerName()).area(sel).worldName(world).claimOwner(player);
            if (name != null) rg.name(name);
            if (!worldMod.addRegion(rg.build())) throw new Command.Error("Could not create claim");
            return "Area claimed!";
        }

        @Command(permission = worldmod.CLAIM, privacy = Command.PrivacyLevel.PRIVATE)
        public static Component info(WorldMod mod, @Nullable Region region) {
            return mod.text().ofRegion(region);
        }

        @Command(permission = worldmod.CLAIM, privacy = Command.PrivacyLevel.PRIVATE)
        public static void menu(WorldMod worldMod, Player player, @Nullable Region region) {
            isClaimed(region);
            var menu = new ClaimMenuBook(worldMod.getLib(), region, player);
            worldMod.getLib().getPlayerAdapter().openBook(player, menu);
        }

        @Command
        public static String name(
                WorldMod worldMod, Player player, @Nullable Region region,
                @Nullable @Command.Arg(stringMode = StringMode.GREEDY, required = false) String arg
        ) {
            isClaimed(region);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player).getState() != TriState.TRUE) notPermitted();
            if (arg == null) arg = NameGenerator.POI.get();
            region.setName(arg);
            worldMod.getEntityService().save(region);
            return "Name was changed to " + arg;
        }

        @Command
        public static String group(WorldMod worldMod, UUID playerId, @Nullable Region region, @Nullable @Command.Arg String arg) {
            throw new Command.Error("Not implemented");
        }

        @Command
        public static String owner(WorldMod worldMod, Player player, @Nullable Region region, @Nullable @Command.Arg String arg) {
            isClaimed(region);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player).getState() != TriState.TRUE) notPermitted();
            if (arg == null) {
                region.setClaimOwner(null);
                return "Owner removed";
            }
            var targetId = worldMod.getLib().getPlayerAdapter().getIdOrThrow(arg);
            var target = worldMod.getLib().getPlayerAdapter().getPlayer(targetId).orElseThrow();
            region.setClaimOwner(target);
            worldMod.getEntityService().save(region);
            return target.getName() + " is now owner of " + region.getBestName();
        }

        @Command
        public static class member {
            @Command
            public static String list(WorldMod worldMod, @Nullable Region region) {
                isClaimed(region);
                return """
                        %sOwners%s: %s
                        %sMembers%s: %s
                        """.formatted(McFormatCode.Aqua,
                        McFormatCode.White,
                        region.getOwners().stream().map(Player::getName).collect(joining(", ")),
                        McFormatCode.Green,
                        McFormatCode.White,
                        region.getMembers().stream().map(Player::getName).collect(joining(", ")));
            }

            @Command
            public static String add(
                    WorldMod worldMod, Player player, @Nullable Region region, @Command.Arg("0") String targetName,
                    @Command.Arg("1") @Nullable @Default("PlayerRelation.MEMBER") PlayerRelation type
            ) {
                if (type == null) type = PlayerRelation.MEMBER;
                isClaimed(region);
                if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player).getState() != TriState.TRUE) notPermitted();
                var targetId = worldMod.getLib().getPlayerAdapter().getIdOrThrow(targetName);
                var target = worldMod.getLib().getPlayerAdapter().getPlayer(targetId).orElseThrow();
                (switch (type) {
                    case MEMBER -> region.getMembers();
                    case ADMIN -> region.getOwners();
                    default -> throw new Command.Error("cannot add member of this type");
                }).add(target);
                return "%s was added to the list of %ss".formatted(player, type.name().toLowerCase());
            }

            @Command
            public static String remove(WorldMod worldMod, Player player, @Nullable Region region, @Command.Arg String targetName) {
                isClaimed(region);
                if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player).getState() != TriState.TRUE) notPermitted();
                var targetId = worldMod.getLib().getPlayerAdapter().getIdOrThrow(targetName);
                var wasOwner  = region.getOwners().remove(targetId);
                var wasMember = region.getMembers().remove(targetId);
                return "%s was removed from the list of %s".formatted(targetName,
                        concat(wasOwner ? of(PlayerRelation.ADMIN) : Stream.empty(), wasMember ? of(PlayerRelation.MEMBER) : Stream.empty()).map(Named::getName)
                                .map(String::toLowerCase)
                                .map(str -> str + 's')
                                .collect(joining(" and ")));
            }
        }

        @Command
        public static class flag {
            @Command
            public static String set(WorldMod worldMod, UUID playerId, @Nullable Region region, @Command.Arg String name, @Command.Arg String value) {
                throw new Command.Error("Not implemented");
            }
        }
    }

    @Command(permission = worldmod.QUERY)
    public static class query {
        @Command(permission = worldmod.QUERY)
        public static Component list(
                WorldMod mod,
                @Command.Arg(required = false, autoFillProvider = AutoFillProvider.WorldNames.class) @Nullable String worldName
        ) {
            var         dash = text("\n - ");
            final int[] i    = new int[]{ 0 };
            return text("Active Queries:").append(dash)
                    .append(mod.getQueryManagers()
                            .entrySet()
                            .stream()
                            .filter(e -> worldName == null || worldName.isBlank() || worldName.equals(e.getKey()))
                            .map(Map.Entry::getValue)
                            .map(IQueryManager::getQueries)
                            .flatMap(Collection::stream)
                            .map(Object::toString)
                            .map(content -> {
                                var shortened = content.length() < MAX_LINE_LENGTH ? content : content.substring(0, MAX_LINE_LENGTH) + "...";
                                return text("").append(text("["))
                                        .append(text(++i[0], YELLOW))
                                        .append(text("|"))
                                        .append(text("#", AQUA).decorate(TextDecoration.BOLD)
                                                .hoverEvent(HoverEvent.showText(text("Update Query...")))
                                                .clickEvent(ClickEvent.suggestCommand("/worldmod:query edit %d %s".formatted(i[0], content))))
                                        .append(text("|"))
                                        .append(text("-", RED).decorate(TextDecoration.BOLD)
                                                .hoverEvent(HoverEvent.showText(text("Remove Query")))
                                                .clickEvent(ClickEvent.suggestCommand(("/worldmod:query remove %d").formatted(i[0]))))
                                        .append(text("] "))
                                        .append(text(shortened, GRAY).hoverEvent(HoverEvent.showText(text(content))));
                            })
                            .collect(Streams.atLeastOneOrElseGet(() -> mod.text().getEmptyListEntry()))
                            .collect(Util.Kyori.collector(dash)));
        }

        @Command(permission = worldmod.QUERY)
        public static Component add(
                WorldMod mod, @Command.Arg(autoFillProvider = AutoFillProvider.WorldNames.class) @NotNull String worldName,
                @Command.Arg(stringMode = StringMode.GREEDY, autoFillProvider = WorldQuery.AutoFillProvider.class) String query
        ) {
            final var parse = WorldQuery.parse(query);
            mod.getQueryManagers()
                    .entrySet()
                    .stream()
                    .filter(e -> worldName == null || worldName.isBlank() || worldName.equals(e.getKey()))
                    .map(Map.Entry::getValue)
                    .map(IQueryManager::getQueries)
                    .forEach(ls -> ls.add(parse));
            return text("Query parsed and added to memory configuration; save with /worldmod:query save", GREEN);
        }

        @Command(permission = worldmod.QUERY)
        public static Component edit(
                WorldMod mod, @Command.Arg(autoFillProvider = AutoFillProvider.WorldNames.class) @NotNull String worldName, @Command.Arg int number,
                @Command.Arg(stringMode = StringMode.GREEDY, autoFillProvider = WorldQuery.AutoFillProvider.class) String query
        ) {
            remove(mod, worldName, number);
            add(mod, worldName, query);
            return text("Query updated", AQUA);
        }

        @Command(permission = worldmod.QUERY)
        public static Component remove(
                WorldMod mod, @Command.Arg(autoFillProvider = AutoFillProvider.WorldNames.class) @NotNull String worldName,
                @Command.Arg int number
        ) {
            return text("Removed " + Word.plural("world query",
                    "\bies",
                    mod.getQueryManagers()
                            .entrySet()
                            .stream()
                            .filter(e -> worldName == null || worldName.isBlank() || worldName.equals(e.getKey()))
                            .map(Map.Entry::getValue)
                            .map(IQueryManager::getQueries)
                            .filter(ls -> ls.size() <= number)
                            .map(ls -> ls.remove(number - 1))
                            .toList()
                            .size()), YELLOW);
        }

        @Command(permission = worldmod.QUERY)
        public static Component clear(
                WorldMod mod,
                @Command.Arg(required = false, autoFillProvider = AutoFillProvider.WorldNames.class) @Nullable String worldName
        ) {
            return text("Removed " + Word.plural("world query",
                    "\bies",
                    mod.getQueryManagers()
                            .entrySet()
                            .stream()
                            .filter(e -> worldName == null || worldName.isBlank() || worldName.equals(e.getKey()))
                            .map(Map.Entry::getValue)
                            .map(IQueryManager::getQueries)
                            .mapToInt(ls -> {
                                var size = ls.size();
                                ls.clear();
                                return size;
                            })
                            .sum()), YELLOW);
        }

        @Command(permission = worldmod.QUERY)
        public static Component save(
                WorldMod mod,
                @Command.Arg(required = false, autoFillProvider = AutoFillProvider.WorldNames.class) @Nullable String worldName
        ) {
            return text("Saved " + Word.plural("query manager",
                    "s",
                    mod.getQueryManagers()
                            .entrySet()
                            .stream()
                            .filter(e -> worldName == null || worldName.isBlank() || worldName.equals(e.getKey()))
                            .map(Map.Entry::getValue)
                            .peek(IQueryManager::save)
                            .toList()
                            .size()), GREEN);
        }
    }
}
