package com.ampznetwork.worldmod.api.model;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;
import org.comroid.api.Polyfill;
import org.comroid.api.data.Vector;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.util.Optional.*;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Value
public class TextResourceProvider {
    WorldMod mod;

    Component EmptyListEntry = text()
            .append(text(" - ").color(WHITE))
            .append(text("(no entries)").color(GRAY))
            .build();

    public Component getLookupHeader(Vector.N3 location) {
        return text()
                .append(text("Recorded events at position ").color(BLUE))
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
        return text()
                .append(text("Use again for ").color(BLUE))
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
        var text = text().append(text()
                        .append(text("[").color(GRAY))
                        .append(text(Polyfill.durationString(Duration.ofMillis(Instant.now()
                                .minusMillis(log.getTimestamp().toEpochMilli())
                                .toEpochMilli()), 2)).color(BLUE))
                        .append(text("]").color(GRAY))
                        .hoverEvent(HoverEvent.showText(text()
                                .append(text(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                        .format(LocalDateTime.ofInstant(log.getTimestamp(), ZoneId.systemDefault())))))))
                .append(text(" "))
                .append(ofState(log.getResult() == null ? TriState.NOT_SET : log.getResult().getEquivalent()))
                .append(text(" "))
                .append(text(log.getAction()).color(AQUA)
                        .hoverEvent(HoverEvent.showText(text(ofNullable(Flag.VALUES.getOrDefault(log.getAction(), null))
                                .map(Flag::getDescription)
                                .orElse("Flag could not be matched\nIs a dependent addon missing?")))))
                .append(text(" @ "))
                .append(text(ofNullable(log.getTarget()).map(Player::getName).orElseGet(log::getNonPlayerTarget)).color(GREEN)
                        .hoverEvent(HoverEvent.showText(text()
                                .append(text("Affected entity")))));
        if (log.getPlayer() != null || log.getNonPlayerSource() != null)
            text.append(text(" (by ")
                    .append(text(ofNullable(log.getPlayer()).map(Player::getName).orElseGet(log::getNonPlayerSource)).color(YELLOW)
                            .hoverEvent(HoverEvent.showText(text()
                                    .append(text("Causing entity")))))
                    .append(text(")")));
        return text.build();
    }

    public Component ofState(TriState triState) {
        // ✅✳❌
        return switch (triState) {
            case NOT_SET -> text("✳").color(YELLOW)
                    .hoverEvent(HoverEvent.showText(text()
                            .append(text("Value was not changed"))));
            case FALSE -> text("❌").color(RED)
                    .hoverEvent(HoverEvent.showText(text()
                            .append(text("Value was forced to be FALSE"))));
            case TRUE -> text("✅").color(GREEN)
                    .hoverEvent(HoverEvent.showText(text()
                            .append(text("Value was forced to be TRUE"))));
        };
    }
}
