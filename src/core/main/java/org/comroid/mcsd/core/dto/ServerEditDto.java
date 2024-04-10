package org.comroid.mcsd.core.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Data
public class ServerEditDto {
    private String name;
    private @Nullable String displayName;
    private @Nullable UUID ownerId;
    private String mcVersion;
    private String host;
    private int port;
    private @Nullable String homepage;
    private boolean managed;
    private boolean enabled;
    private boolean whitelist;
    private int queryPort;
    private int rConPort;
    private @Nullable String rConPassword;
    private @Nullable UUID discordBotId;
    private @Nullable Long publicChannelId;
    private @Nullable Long moderationChannelId;
    private @Nullable Long consoleChannelId;
    private @Nullable String consoleChannelPrefix;
    private boolean fancyConsole;
}
