package com.ampznetwork.worldmod.api.model;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.comroid.api.Polyfill;

import java.time.Duration;
import java.time.Instant;

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
    Component LookupHeader   = text()
            .append(text("The following events have been recorded:").color(BLUE))
            .build();

    public Component ofState(TriState triState) {
        // ✅✳❌
        return switch (triState) {
            case NOT_SET -> text("✳").color(YELLOW);
            case FALSE -> text("❌").color(RED);
            case TRUE -> text("✅").color(GREEN);
        };
    }

    @SuppressWarnings("DataFlowIssue")
    public Component ofLookupEntry(LogEntry log) {
        // [$DATETIME] $STATE $action @ $target (for source)?
        var text = text().append(text()
                        .append(text("[").color(GRAY))
                        .append(text(Polyfill.durationString(Duration.ofMillis(Instant.now()
                                .minusMillis(log.getTimestamp().toEpochMilli())
                                .toEpochMilli()), 2)).color(BLUE))
                        .append(text("]").color(GRAY)))
                .append(text(" "))
                .append(ofState(log.getResult() == null ? TriState.NOT_SET : log.getResult().getEquivalent()))
                .append(text(" "))
                .append(text(log.getAction()).color(AQUA))
                .append(text(" @ "))
                .append(text(ofNullable(log.getTarget()).map(Player::getName).orElseGet(log::getNonPlayerTarget)).color(GREEN));
        if (log.getPlayer() != null || log.getNonPlayerSource() != null)
            text.append(text(" (by "))
                    .append(text(ofNullable(log.getPlayer()).map(Player::getName).orElseGet(log::getNonPlayerSource)).color(YELLOW))
                    .append(text(")"));
        return text.build();
    }
}
