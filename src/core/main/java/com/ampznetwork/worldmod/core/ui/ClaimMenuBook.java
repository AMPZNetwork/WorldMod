package com.ampznetwork.worldmod.core.ui;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.adp.BookAdapter;
import com.ampznetwork.worldmod.api.model.mini.PlayerRelation;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.*;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static org.comroid.api.func.util.Streams.atLeastOneOrElseGet;
import static org.comroid.api.func.util.Streams.cast;
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
                concat(members(), flags()))
                .toList();
    }

    private Component[] toc() {
        var pgFlags = 3 + (int) members().count();
        return new Component[]{text("""
                %s Menu
                %s by %s
                \s
                \s
                """.formatted(
                region.getClaimOwner() == null ? "Region" : "Claim",
                region.getName(),
                region.getClaimOwner() == null
                        ? region.getWorldName()
                        : worldMod.getPlayerAdapter().getName(region.getClaimOwner()))),
                text("2 ... Claim Details\n")
                        .decorate(TextDecoration.UNDERLINED)
                        .hoverEvent(showText(text("Jump to page")))
                        .clickEvent(changePage(2)),
                text("3 ... Claim Members\n")
                        .decorate(TextDecoration.UNDERLINED)
                        .hoverEvent(showText(text("Jump to page")))
                        .clickEvent(changePage(3)),
                text(pgFlags + " ... Flags\n")
                        .decorate(TextDecoration.UNDERLINED)
                        .hoverEvent(showText(text("Jump to page")))
                        .clickEvent(changePage(pgFlags))
        };
    }

    private Component[] details() {
        return new Component[]{text("""
                %s Details
                \s
                Name: %s
                World: %s
                \s
                Group: %s
                Owner: %s""".formatted(
                region.getClaimOwner() == null ? "Region" : "Claim",
                region.getBestName(),
                region.getWorldName(),
                region.getGroup() == null ? "none" : region.getGroup().getBestName(),
                region.getClaimOwner() == null ? "none" : worldMod.getPlayerAdapter().getName(region.getClaimOwner())
        ))};
    }

    private Stream<Component[]> members() {
        final int pageLimit = 11;
        final var helper = new Object() {
            TextComponent header(PlayerRelation type) {
                var typeNameTitleCase = Title_Case.convert(type.name());
                return text()
                        .append(text("[+]")
                                .color(NamedTextColor.GREEN)
                                .hoverEvent(showText(text("Add " + typeNameTitleCase)))
                                .clickEvent(suggestCommand("/region member add \u0001 " + type.name().toLowerCase())))
                        .append(text(" %s List\n".formatted(typeNameTitleCase))
                                .color(NamedTextColor.BLACK))
                        .decorate(TextDecoration.UNDERLINED)
                        .build();
            }

            List<@NotNull TextComponent> entries(PlayerRelation type) {
                var typeNameTitleCase = Title_Case.convert(type.name());
                return (switch (type) {
                    case MEMBER -> region.getMemberIDs();
                    case OWNER -> region.getOwnerIDs();
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                }).stream()
                        .map(id -> {
                            var name = worldMod.getPlayerAdapter().getName(id);
                            return text()
                                    .append(text("[-]")
                                            .color(NamedTextColor.RED)
                                            .hoverEvent(showText(text("Remove %s '%s'".formatted(typeNameTitleCase, name))))
                                            .clickEvent(runCommand("/region member remove " + name)))
                                    .append(text(" %s\n".formatted(name))
                                            .color(NamedTextColor.BLACK))
                                    .build();
                        })
                        .collect(atLeastOneOrElseGet(() -> text("- no %ss -".formatted(type.name().toLowerCase()))
                                .decorate(TextDecoration.ITALIC)))
                        .toList();
            }
        };

        var pages = new ArrayList<TextComponent>();
        TextComponent page = null;
        int pageSize = 0;
        var categories = of(PlayerRelation.OWNER, PlayerRelation.MEMBER)
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

    private Stream<Component[]> flags() {
        return of((Object) new Component[]{text("flags (wip)")}).flatMap(cast(Component[].class));
    }
}
