package org.comroid.mcsd.api.dto.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor
public class AgentInfo {
    @NotNull UUID target;
    @NotNull UUID agent;
    @Deprecated @NotNull String hubBaseUrl;
    @NotNull String token;
    @Nullable String baseUrl;
}
