package org.comroid.mcsd.api.dto.comm;

import lombok.*;
import org.comroid.mcsd.api.model.Status;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class StatusMessage {
    private final Instant timestamp = Instant.now();
    private final @NotNull UUID targetId;
    private @With @lombok.Builder.Default Status status = Status.unknown_status;
    private @With @lombok.Builder.Default @Nullable Status rcon = Status.unknown_status;
    private @With @lombok.Builder.Default @Nullable Status ssh = Status.unknown_status;
    private @With @lombok.Builder.Default int playerCount = 0;
    private @With @lombok.Builder.Default int playerMax = 0;
    private @With @Nullable String motd;
    private @With @Nullable List<String> players;
    private @With @Nullable String gameMode;
    private @With @Nullable String worldName;
    private @With @Nullable UUID userId;

    public @Nullable String getMotdSanitized() {
        return motd == null ? null : motd.replaceAll("[&§�]\\w", "");
    }

    public StatusMessage combine(@Nullable StatusMessage other) {
        if (other == null)
            return this;
        if (!targetId.equals(other.targetId))
            throw new IllegalArgumentException("Server IDs must be equal");
        if (other.players == null)
            other = other.withPlayers(players);
        if (other.gameMode == null)
            other = other.withGameMode(gameMode);
        if (other.worldName == null)
            other = other.withWorldName(worldName);
        return other;
    }
}
