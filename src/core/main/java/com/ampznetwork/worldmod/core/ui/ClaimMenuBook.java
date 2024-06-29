package com.ampznetwork.worldmod.core.ui;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.BookAdapter;
import com.ampznetwork.worldmod.api.model.mini.PlayerRelation;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.util.TriState;
import org.comroid.api.data.RegExpUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Stream.*;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.*;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;
import static org.comroid.api.func.util.Streams.atLeastOneOrElseGet;
import static org.comroid.api.func.util.Streams.groupingEvery;
import static org.comroid.api.text.Capitalization.Title_Case;

@Value
public class ClaimMenuBook implements BookAdapter {
    WorldMod worldMod;
    Region region;
    UUID playerId;

    @Override
    public List<Component[]> getPages() {
        return concat(
                of(toc(), details()),
                concat(members(),
                        concat(flags$toc(), flags$pages())))
                .toList();
    }

    private Component[] toc() {
        var pgFlags = 3 + (int) members().count();
        var claimOwner = region.getClaimOwner();
        var bestName = region.getBestName();
        var rcTitle = region.getClaimOwner() == null ? "Region" : "Claim";

        return new Component[]{
                text("%s Menu\n".formatted(rcTitle))
                        .decorate(UNDERLINED),
                text("%s by %s\n".formatted(bestName.matches(RegExpUtil.UUID4_PATTERN)
                        ? "Unnamed " + rcTitle
                        : bestName, worldMod.getPlayerAdapter().getName(claimOwner))),
                text("\n"),
                text("\n"),
                text("2 ... ")
                        .append(text("Claim Details\n")
                        .decorate(UNDERLINED)
                        .hoverEvent(showText(text("Jump to page")))
                        .clickEvent(changePage(2))),
                text("3 ... ")
                        .append(text("Claim Members\n")
                        .decorate(UNDERLINED)
                        .hoverEvent(showText(text("Jump to page")))
                        .clickEvent(changePage(3))),
                text(pgFlags + " ... ")
                        .append(text("Flags\n")
                        .decorate(UNDERLINED)
                        .hoverEvent(showText(text("Jump to page")))
                        .clickEvent(changePage(pgFlags)))
        };
    }

    private Component[] details() {
        var claimOwner = region.getClaimOwner();
        var bestName = region.getBestName();
        var group = region.getGroup();
        var canManage = region.getEffectiveFlagValueForPlayer(Flag.Manage, playerId).getState() == TriState.TRUE;

        var compName = text("Name: %s".formatted(bestName.matches(RegExpUtil.UUID4_PATTERN)
                ? "<not set>" : bestName));
        var compGroup = text("Group: %s".formatted(group == null
                ? "none" : group.getBestName()));
        var compOwner = text("Owner: %s".formatted(claimOwner == null
                ? "none" : worldMod.getPlayerAdapter().getName(claimOwner)));

        if (canManage) {
            compName = compName
                    .append(text(" "))
                    .append(text("#")
                            .color(DARK_AQUA)
                            .clickEvent(suggestCommand("/region name "))
                            .hoverEvent(showText(text("Change Name"))));
            compGroup = compGroup
                    .append(text(" "))
                    .append(text("#")
                            .color(DARK_AQUA)
                            .clickEvent(suggestCommand("/region group "))
                            .hoverEvent(showText(text("Change Group"))));
            compOwner = compOwner
                    .append(text(" "))
                    .append(text("#")
                            .color(DARK_AQUA)
                            .clickEvent(suggestCommand("/region owner "))
                            .hoverEvent(showText(text("Transfer Ownership")
                                    .color(RED))));
        }
        compName = compName.append(text("\n"));
        compGroup = compGroup.append(text("\n"));
        compOwner = compOwner.append(text("\n"));

        return new Component[]{
                text("%s Details\n".formatted(claimOwner == null ? "Region" : "Claim"))
                        .decorate(UNDERLINED),
                text("\n"),
                compName,
                text("World: %s\n".formatted(region.getWorldName())),
                text("\n"),
                compGroup,
                compOwner
        };
    }

    private Stream<Component[]> members() {
        final int pageLimit = 11;
        final var helper = new Object() {
            TextComponent header(PlayerRelation type) {
                var typeNameTitleCase = Title_Case.convert(type.name());
                return text()
                        .append(text("[+]")
                                .color(GREEN)
                                .hoverEvent(showText(text("Add " + typeNameTitleCase)))
                                .clickEvent(suggestCommand("/region member add \u0001 " + type.name().toLowerCase())))
                        .append(text(" "))
                        .append(text("%s List\n".formatted(typeNameTitleCase))
                                .color(BLACK)
                                .decorate(UNDERLINED))
                        .build();
            }

            List<@NotNull TextComponent> entries(PlayerRelation type) {
                var typeNameTitleCase = Title_Case.convert(type.name());
                return (switch (type) {
                    case MEMBER -> region.getMemberIDs();
                    case ADMIN -> region.getOwnerIDs();
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                }).stream()
                        .map(id -> {
                            var name = worldMod.getPlayerAdapter().getName(id);
                            return text()
                                    .append(text("[-]")
                                            .color(RED)
                                            .hoverEvent(showText(text("Remove %s '%s'".formatted(typeNameTitleCase, name))))
                                            .clickEvent(runCommand("/region member remove " + name)))
                                    .append(text(" %s\n".formatted(name))
                                            .color(BLACK))
                                    .build();
                        })
                        .collect(atLeastOneOrElseGet(() -> text("- no %ss -".formatted(type.name().toLowerCase()))
                                .decorate(ITALIC)))
                        .toList();
            }
        };

        var pages = new ArrayList<TextComponent>();
        TextComponent page = null;
        int pageSize = 0;
        var categories = of(PlayerRelation.ADMIN, PlayerRelation.MEMBER)
                .map(type -> new AbstractMap.SimpleImmutableEntry<>(type, helper.entries(type).iterator()))
                .iterator();

        while (categories.hasNext()) {
            var category = categories.next();
            if (page == null)
                page = text().build();
            var header = helper.header(category.getKey());
            page = page.append(header);
            var entries = category.getValue();

            while (entries.hasNext()) {
                var entry = entries.next();
                page = page.append(entry);
                pageSize += 1;
            }

            page = page.append(text("\n"));
            if (pageSize >= pageLimit) {
                pages.add(page);
                page = null;
            }
        }
        if (page != null)
            pages.add(page);

        return pages.stream().map(comp -> new Component[]{comp});
    }

    private Stream<Component[]> flags$toc() {
        record FlagEntry(Flag flag, int level) {
            public static Stream<FlagEntry> expand(Flag flag, int i) {
                return concat(
                        of(new FlagEntry(flag, i)),
                        flag.getChildren().stream()
                                .flatMap(child -> expand(child, i + 1)));
            }
        }
        final var header = text("Flags Menu\n\n")
                .decorate(UNDERLINED);
        final var entriesPerPage = 10;
        final var pgFlags = (int) members().count();
        final var lenFlagToc = Flag.VALUES.size() / entriesPerPage;
        final var flagPageOffset = pgFlags + lenFlagToc;
        final var pageOffsetCounter = new AtomicInteger(0);
        return Flag.VALUES.values().stream()
                .filter(flag -> flag.getParent() == null) // only root flags here
                .sorted(Flag.COMPARATOR)
                .collect(groupingEvery(entriesPerPage)).stream()
                .map(page -> {
                    var comp = text().append(header);
                    page.stream()
                            .flatMap(flag -> FlagEntry.expand(flag, 0))
                            .sorted(Comparator.comparing(FlagEntry::flag, Flag.COMPARATOR))
                            .forEach(entry -> {
                                var pageNo = flagPageOffset + pageOffsetCounter.getAndIncrement();

                                comp.append(text("%03d ".formatted(pageNo) + IntStream.range(1, entry.level + 2)
                                        .mapToObj($ -> "- ")
                                        .collect(Collectors.joining()))
                                        .append(text(entry.flag.getBestName())
                                                .decorate(UNDERLINED)
                                                .clickEvent(changePage(pageNo))
                                                .font(Key.key("minecraft", "uniform"))
                                                .hoverEvent(showText(text("Jump to Page " + pageNo))))
                                        .append(text("\n")));
                            });
                    return new Component[]{comp.build()};
                });
    }

    private Stream<Component[]> flags$pages() {
        return empty();
    }
}
