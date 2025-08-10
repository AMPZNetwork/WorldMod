package com.ampznetwork.worldmod.core;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.API;
import com.ampznetwork.libmod.api.model.AutoFillProvider;
import com.ampznetwork.libmod.api.util.NameGenerator;
import com.ampznetwork.libmod.api.util.Util;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.mini.PlayerRelation;
import com.ampznetwork.worldmod.api.model.query.IQueryManager;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.core.model.AutoFillProvider.Flags;
import com.ampznetwork.worldmod.core.model.AutoFillProvider.Groups;
import com.ampznetwork.worldmod.core.model.AutoFillProvider.Regions;
import com.ampznetwork.worldmod.core.model.AutoFillProvider.RegionsAndGroups;
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

import static com.ampznetwork.libmod.api.util.chat.BroadcastType.*;
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
    public Component select(WorldMod mod, UUID playerId, @Command.Arg @Nullable Shape type) {
        if (type == null) {
            selections.remove(playerId);
            return mod.chat().createMessage(HINT, "Selection cleared");
        }
        sel(playerId).shape(type);
        clearSel(playerId);
        return mod.chat().createMessage("Now selecting {} shapes", type.name());
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
        public static Component $(WorldMod mod, UUID playerId, @Command.Arg(autoFill = { "1", "2" }) int index) {
            var pos = mod.getLib().getPlayerAdapter().getPosition(playerId);
            if (index == 1) sel(playerId).x1((int) pos.getX()).y1((int) pos.getY()).z1((int) pos.getZ());
            else if (index == 2) sel(playerId).x2((int) pos.getX()).y2((int) pos.getY()).z2((int) pos.getZ());
            return mod.chat().createMessage("Set position {}", index);
        }

        @Command
        public static Component clear(WorldMod mod, UUID playerId) {
            clearSel(playerId);
            return mod.chat().createMessage(HINT, "Selection cleared");
        }
    }

    @Command
    @Alias("region")
    public static class claim {
        @Command(permission = worldmod.CLAIM, privacy = Command.PrivacyLevel.PRIVATE)
        public static Component create(
                WorldMod mod, UUID playerId,
                @Nullable @Command.Arg(required = false) String name
        ) {
            var player = mod.getLib().getPlayerAdapter().getPlayer(playerId).orElseThrow();
            if (!selections.containsKey(playerId)) return mod.chat().createMessage(ERROR, "No area selected");
            var sel = sel(playerId).build();
            sel.validateShapeMask();
            var world = mod.getLib().getPlayerAdapter().getWorldName(playerId);
            var rg = Region.builder()
                    .serverName(mod.getLib().getServerName())
                    .area(sel)
                    .worldName(world)
                    .claimOwner(player);
            if (name != null) rg.name(name);
            if (!mod.addRegion(rg.build())) return mod.chat().createMessage(FATAL, "Could not create claim");
            return mod.chat().createMessage("Area claimed!");
        }

        @Command(permission = worldmod.CLAIM, privacy = Command.PrivacyLevel.PRIVATE)
        public static Component info(
                WorldMod mod, UUID playerId, @Nullable Region region0,
                @Nullable @Command.Arg(required = false, autoFillProvider = Regions.class) String regionName
        ) {
            var region = region0 == null ? Region.global(mod.getPlayerAdapter().getWorldName(playerId)) : region0;
            return mod.text().ofRegion(region);
        }

        @Command(permission = worldmod.CLAIM, privacy = Command.PrivacyLevel.PRIVATE)
        public static void menu(
                WorldMod mod, Player player, @Nullable Region region0,
                @Nullable @Command.Arg(required = false, autoFillProvider = Regions.class) String regionName
        ) {
            var region = requireRegion(mod, region0, regionName);
            isClaimed(region);
            var menu = new ClaimMenuBook(mod.getLib(), region, player);
            mod.getLib().getPlayerAdapter().openBook(player, menu);
        }

        @Command
        public static Component name(
                WorldMod mod, Player player, @Nullable Region region0,
                @Nullable @Command.Arg(required = false, autoFillProvider = Regions.class) String regionName,
                @Nullable @Command.Arg(stringMode = StringMode.GREEDY, required = false) String newName
        ) {
            var region = requireRegion(mod, region0, regionName);
            isClaimed(region);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player) != TriState.TRUE) notPermitted();
            if (newName == null) newName = NameGenerator.POI.get();
            region.setName(newName);
            mod.getEntityService().save(region);
            return mod.chat().createMessage("Region name was changed to {}", newName);
        }

        @Command
        public static Component group(
                WorldMod mod, Player player, @Nullable Region region0,
                @Nullable @Command.Arg(required = false, autoFillProvider = Groups.class) String groupName,
                @Nullable @Command.Arg(required = false, autoFillProvider = Regions.class) String regionName
        ) {
            var region = requireRegion(mod, region0, regionName);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player) != TriState.TRUE) notPermitted();

            try {
                if (groupName == null) {
                    // unset
                    region.setGroup(null);
                    return mod.chat().createMessage(HINT, "Region {} had its group {}", region.getName(), "removed");
                }

                // find and set
                var select = mod.getEntityAccessor(Group.TYPE).by(DbObject.WithName::getName).get(groupName);
                if (select.isEmpty())
                    return mod.chat().createMessage(ERROR, "Group with name '{}' was not found", groupName);
                region.setGroup(select.get());
                return mod.chat().createMessage("Region {} had its group set to {}", region.getName(), groupName);
            } finally {
                mod.getEntityService().save(region);
            }
        }

        @Command
        public static String owner(
                WorldMod mod, Player player, @Nullable Region region0, @Nullable @Command.Arg String newOwnerName,
                @Nullable @Command.Arg(required = false, autoFillProvider = Regions.class) String regionName
        ) {
            var region = requireRegion(mod, region0, regionName);
            isClaimed(region);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player) != TriState.TRUE) notPermitted();

            try {
                if (newOwnerName == null) {
                    region.setClaimOwner(null);
                    return "Owner removed";
                }
                var targetId = mod.getLib().getPlayerAdapter().getIdOrThrow(newOwnerName);
                var target   = mod.getLib().getPlayerAdapter().getPlayer(targetId).orElseThrow();
                region.setClaimOwner(target);
                return target.getName() + " is now owner of " + region.getBestName();
            } finally {
                mod.getEntityService().save(region);
            }
        }

        @Command
        public static Component flag(
                WorldMod mod, UUID playerId, @Nullable Region region0,
                @Command.Arg(autoFillProvider = Flags.class) String flagName,
                @Nullable @Command.Arg(required = false) TriState state,
                @Nullable @Command.Arg(required = false, autoFillProvider = RegionsAndGroups.class) String regionName
        ) {
            var region = requireRegion(mod, region0, regionName);
            var flag   = Flag.getForName(flagName);
            if (flag == null) throw new Command.Error("No such flag: " + flagName);
            if (state == null) state = TriState.byBoolean(flag.isAllowByDefault());

            final var fState  = state;
            final var removed = new boolean[1];
            var       usage   = new Flag.Usage(flag, 0, state, 0, false);
            mod.getEntityAccessor(Region.TYPE).update(region, rg -> {
                var flags = rg.getDeclaredFlags();
                removed[0] = flags.remove(usage) && fState == TriState.NOT_SET;
                if (fState != TriState.NOT_SET) flags.add(usage);
            });

            return mod.chat()
                    .createMessage(HINT,
                            "Flag {} configuration {} in region {}",
                            usage,
                            removed[0] ? "removed" : "set to " + fState.name(),
                            region.getName());
        }

        private static Region requireRegion(API api, @Nullable Region region, @Nullable String regionName) {
            if ((region == null || region.isGlobal())) {
                if (regionName == null)
                    throw new Command.Error("You must either provide a region name or be inside a region");
                return api.getEntityAccessor(Region.TYPE)
                        .by(Region::getName)
                        .get(regionName)
                        .orElseThrow(() -> new Command.Error("No such region: " + regionName));
            }
            return region;
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
            public static Component add(
                    WorldMod mod, Player player, @Nullable Region region, @Command.Arg("0") String targetName,
                    @Command.Arg("1") @Nullable @Default("PlayerRelation.MEMBER") PlayerRelation type
            ) {
                if (type == null) type = PlayerRelation.MEMBER;
                isClaimed(region);
                if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player) != TriState.TRUE) notPermitted();
                var targetId = mod.getLib().getPlayerAdapter().getIdOrThrow(targetName);
                var target   = mod.getLib().getPlayerAdapter().getPlayer(targetId).orElseThrow();

                try {
                    switch (type) {
                        case MEMBER:
                            region.getMembers().add(target);
                            break;
                        case ADMIN:
                            region.getOwners().add(target);
                            break;
                        default:
                            return mod.chat().createMessage(FATAL, "Cannot add member of type {}", type);
                    }
                    return mod.chat()
                            .createMessage("{} was added to the list of {}s", player, type.name().toLowerCase());
                } finally {
                    mod.getEntityService().save(region);
                }
            }

            @Command
            public static Component remove(
                    WorldMod mod, Player player, @Nullable Region region,
                    @Command.Arg String targetName
            ) {
                isClaimed(region);
                if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player) != TriState.TRUE) notPermitted();
                var select = mod.getLib().getPlayerAdapter().getPlayer(targetName);
                if (select.isEmpty())
                    return mod.chat().createMessage(ERROR, "Player with name '{}' could not be found");
                var target    = select.get();
                var wasOwner  = region.getOwners().remove(target);
                var wasMember = region.getMembers().remove(target);
                mod.getEntityService().save(region);
                return mod.chat()
                        .createMessage(HINT,
                                "{} was removed from the list of {}",
                                targetName,
                                concat(wasOwner ? of(PlayerRelation.ADMIN) : Stream.empty(),
                                        wasMember ? of(PlayerRelation.MEMBER) : Stream.empty()).map(Named::getName)
                                        .map(String::toLowerCase)
                                        .map(str -> str + 's')
                                        .collect(joining(" and ")));
            }
        }
    }

    @Command(permission = worldmod.QUERY)
    public static class query {
        @Command(permission = worldmod.QUERY)
        public static Component list(
                WorldMod mod, @Command.Arg(required = false,
                                           autoFillProvider = AutoFillProvider.WorldNames.class) @Nullable String worldName
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
                                var shortened = content.length() < MAX_LINE_LENGTH
                                                ? content
                                                : content.substring(0, MAX_LINE_LENGTH) + "...";
                                return text("").append(text("["))
                                        .append(text(++i[0], YELLOW))
                                        .append(text("|"))
                                        .append(text("#", AQUA).decorate(TextDecoration.BOLD)
                                                .hoverEvent(HoverEvent.showText(text("Update Query...")))
                                                .clickEvent(ClickEvent.suggestCommand("/worldmod:query edit %d %s".formatted(
                                                        i[0],
                                                        content))))
                                        .append(text("|"))
                                        .append(text("-", RED).decorate(TextDecoration.BOLD)
                                                .hoverEvent(HoverEvent.showText(text("Remove Query")))
                                                .clickEvent(ClickEvent.suggestCommand(("/worldmod:query remove %d").formatted(
                                                        i[0]))))
                                        .append(text("] "))
                                        .append(text(shortened, GRAY).hoverEvent(HoverEvent.showText(text(content))));
                            })
                            .collect(Streams.atLeastOneOrElseGet(() -> mod.text().getEmptyListEntry()))
                            .collect(Util.Kyori.collector(dash)));
        }

        @Command(permission = worldmod.QUERY)
        public static Component add(
                WorldMod mod,
                @Command.Arg(autoFillProvider = AutoFillProvider.WorldNames.class) @NotNull String worldName,
                @Command.Arg(stringMode = StringMode.GREEDY,
                             autoFillProvider = WorldQuery.AutoFillProvider.class) String query
        ) {
            final var parse = WorldQuery.parse(query);
            mod.getQueryManagers()
                    .entrySet()
                    .stream()
                    .filter(e -> worldName == null || worldName.isBlank() || worldName.equals(e.getKey()))
                    .map(Map.Entry::getValue)
                    .map(IQueryManager::getQueries)
                    .forEach(ls -> ls.add(parse));
            return mod.chat()
                    .createMessage(HINT,
                            "Query parsed and added to memory configuration; save with {}",
                            "/worldmod:query save");
        }

        @Command(permission = worldmod.QUERY)
        public static Component edit(
                WorldMod mod,
                @Command.Arg(autoFillProvider = AutoFillProvider.WorldNames.class) @NotNull String worldName,
                @Command.Arg int number, @Command.Arg(stringMode = StringMode.GREEDY,
                                                      autoFillProvider = WorldQuery.AutoFillProvider.class) String query
        ) {
            remove(mod, worldName, number);
            add(mod, worldName, query);
            return mod.chat().createMessage("Query updated");
        }

        @Command(permission = worldmod.QUERY)
        public static Component remove(
                WorldMod mod,
                @Command.Arg(autoFillProvider = AutoFillProvider.WorldNames.class) @NotNull String worldName,
                @Command.Arg int number
        ) {
            var count = mod.getQueryManagers()
                    .entrySet()
                    .stream()
                    .filter(e -> worldName == null || worldName.isBlank() || worldName.equals(e.getKey()))
                    .map(Map.Entry::getValue)
                    .map(IQueryManager::getQueries)
                    .filter(ls -> ls.size() <= number)
                    .map(ls -> ls.remove(number - 1))
                    .toList()
                    .size();
            return mod.chat().createMessage(HINT, "Removed {} world " + Word.plural("query", "\bies", count));
        }

        @Command(permission = worldmod.QUERY)
        public static Component clear(
                WorldMod mod, @Command.Arg(required = false,
                                           autoFillProvider = AutoFillProvider.WorldNames.class) @Nullable String worldName
        ) {
            var count = mod.getQueryManagers()
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
                    .sum();
            return mod.chat().createMessage(HINT, "Cleared {} world " + Word.plural("query", "\bies", count));
        }

        @Command(permission = worldmod.QUERY)
        public static Component save(
                WorldMod mod, @Command.Arg(required = false,
                                           autoFillProvider = AutoFillProvider.WorldNames.class) @Nullable String worldName
        ) {
            var count = mod.getQueryManagers()
                    .entrySet()
                    .stream()
                    .filter(e -> worldName == null || worldName.isBlank() || worldName.equals(e.getKey()))
                    .map(Map.Entry::getValue)
                    .peek(IQueryManager::save)
                    .mapToInt(mgr -> mgr.getQueries().size())
                    .sum();
            return mod.chat().createMessage(HINT, "Saved {} world " + Word.plural("query", "\bies", count));
        }
    }
}
