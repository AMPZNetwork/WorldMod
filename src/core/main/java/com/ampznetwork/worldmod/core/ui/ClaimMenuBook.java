package com.ampznetwork.worldmod.core.ui;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.adp.BookAdapter;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.changePage;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static org.comroid.api.func.util.Streams.cast;

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
        //ClickEvent.suggestCommand("\u0001 64") <- move cursor to position
        return of((Object) new Component[]{text("members (wip)")}).flatMap(cast(Component[].class));
    }

    private Stream<Component[]> flags() {
        return of((Object) new Component[]{text("flags (wip)")}).flatMap(cast(Component[].class));
    }
}
