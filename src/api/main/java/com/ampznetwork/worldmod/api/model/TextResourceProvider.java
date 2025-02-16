package com.ampznetwork.worldmod.api.model;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;
import org.comroid.api.Polyfill;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.util.Optional.*;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.HoverEvent.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Value
public class TextResourceProvider {
    WorldMod mod;

    Component EmptyListEntry = text().append(text(" - ").color(WHITE)).append(text("(no entries)").color(GRAY)).build();

    Component NoClaimMessage = text("This area is wilderness", GREEN);

    public Component getLookupHeader(Vector.N3 location) {
        return text().append(text("Recorded events at position ").color(BLUE))
                .append(text("(").color(GRAY))
                .append(text(location.getX()).color(AQUA))
                .append(text(", ").color(GRAY))
                .append(text(location.getY()).color(AQUA))
                .append(text(", ").color(GRAY))
                .append(text(location.getZ()).color(AQUA))
                .append(text(")").color(GRAY))
                .build();
    }

    public Component getLookupFooter(int page, int totalPages) {
        return text().append(text("Use again for ").color(BLUE))
                .append(text(page == totalPages ? "first page" : "next page").color(BLUE))
                .append(text("; hold SHIFT for reverse").color(BLUE))
                .append(text(" (").color(GRAY))
                .append(text(page).color(AQUA))
                .append(text("/").color(GRAY))
                .append(text(totalPages).color(AQUA))
                .append(text(")").color(GRAY))
                .decorate(TextDecoration.ITALIC)
                .build();
    }

    @SuppressWarnings("DataFlowIssue")
    public Component ofLookupEntry(LogEntry log) {
        // [$DATETIME] $STATE $action @ $target (for source)?
        var text = text().append(text().append(text("[").color(GRAY))
                        .append(text(Polyfill.durationString(Duration.ofMillis(Instant.now().minusMillis(log.getTimestamp().toEpochMilli()).toEpochMilli()), 2)).color(
                                BLUE))
                        .append(text("]").color(GRAY))
                        .hoverEvent(showText(text().append(text(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                .format(LocalDateTime.ofInstant(log.getTimestamp(), ZoneId.systemDefault())))))))
                .append(text(" "))
                .append(ofState(log.getResult() == null ? TriState.NOT_SET : log.getResult().getEquivalent()))
                .append(text(" "))
                .append(text(log.getAction()).color(AQUA)
                        .hoverEvent(showText(text(ofNullable(Flag.VALUES.getOrDefault(log.getAction(), null)).map(Flag::getDescription)
                                .orElse("Flag could not be matched\nIs a dependent addon missing?")))))
                .append(text(" @ "))
                .append(text(ofNullable(log.getTarget()).map(Player::getName).orElseGet(log::getNonPlayerTarget)).color(GREEN)
                        .hoverEvent(showText(text().append(text("Affected entity")))));
        if (log.getPlayer() != null || log.getNonPlayerSource() != null) text.append(text(" (by ").append(text(ofNullable(log.getPlayer()).map(Player::getName)
                .orElseGet(log::getNonPlayerSource)).color(YELLOW).hoverEvent(showText(text().append(text("Causing entity"))))).append(text(")")));
        return text.build();
    }

    public Component ofState(TriState triState) {
        // ✅✳❌
        return switch (triState) {
            case NOT_SET -> text("✳").color(YELLOW).hoverEvent(showText(text().append(text("Value was not changed"))));
            case FALSE -> text("❌").color(RED).hoverEvent(showText(text().append(text("Value was forced to be FALSE"))));
            case TRUE -> text("✅").color(GREEN).hoverEvent(showText(text().append(text("Value was forced to be TRUE"))));
        };
    }

    public Component getRegionHeader(Region rg) {
        return text("=[ ", YELLOW).append(text(rg.getClaimOwner() == null ? "Region" : "Claim", YELLOW, TextDecoration.UNDERLINED))
                .append(text(" \"", GRAY, TextDecoration.UNDERLINED))
                .append(legacyAmpersand().deserialize(rg.getName()).decorate(TextDecoration.UNDERLINED))
                .append(text("\"", GRAY, TextDecoration.UNDERLINED))
                .append(text(" by ", YELLOW, TextDecoration.UNDERLINED))
                .append(text(rg.getClaimOwner() == null ? "Server" : rg.getClaimOwner().getName(),
                        rg.getClaimOwner() == null ? GREEN : BLUE,
                        TextDecoration.UNDERLINED))
                .append(text(" ]=", YELLOW, TextDecoration.UNDERLINED));
    }

    public Component getRegionDetails(Region rg) {
        return text(rg.getOwners().size(), AQUA).append(text(" Admins", RED, TextDecoration.UNDERLINED).hoverEvent(showText(text("Open Admin menu...")))
                        .clickEvent(ClickEvent.runCommand("/chatmod:region menu admins")))
                .append(text(", ", GRAY))
                .append(text(rg.getMembers().size(), AQUA))
                .append(text(" Members", GREEN, TextDecoration.UNDERLINED).hoverEvent(showText(text("Open Member menu...")))
                        .clickEvent(ClickEvent.runCommand("/chatmod:region menu members")))
                .append(text(", ", GRAY))
                .append(text(Flag.VALUES.values().stream().filter(flag -> rg.getFlagState(flag) != TriState.NOT_SET).count(), AQUA))
                .append(text(" Flags", YELLOW, TextDecoration.UNDERLINED).hoverEvent(showText(text("Open Flags menu...")))
                        .clickEvent(ClickEvent.runCommand("/chatmod:region menu flags")))
                .append(text(", ", GRAY))
                .append(text(rg.getQueries().size(), AQUA))
                .append(text(" Queries", LIGHT_PURPLE, TextDecoration.UNDERLINED).hoverEvent(showText(text("Open Query menu...")))
                        .clickEvent(ClickEvent.runCommand("/chatmod:region menu queries")));
    }

    public Component getRegionMenu(Region rg) {
        return text("Visualizer ", GOLD).hoverEvent(showText(text("Work in Progress"))).append(text("WIP", DARK_PURPLE));
    }

    public Component ofRegion(@Nullable Region rg) {
        return rg == null ? NoClaimMessage : getRegionHeader(rg).append(text("\n")).append(getRegionDetails(rg)).append(text("\n")).append(getRegionMenu(rg));
    }
}
