package org.comroid.mcsd.core.entity.module.discord;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.comroid.mcsd.core.entity.module.ModulePrototype;
import org.comroid.mcsd.core.entity.system.DiscordBot;
import org.jetbrains.annotations.Nullable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscordModulePrototype extends ModulePrototype {
    public static final long DefaultPublicChannelEvents = 0xFFFF_FFFF_FFFF_FFFFL;

    private @Nullable @ManyToOne DiscordBot discordBot;
    /**
     * @deprecated should always fetch by name or create, then cache and handle live deletion
     */
    private @Nullable @Deprecated String publicChannelWebhook;
    private @Nullable @Column(unique = true) Long publicChannelId;
    private @Nullable Long moderationChannelId;
    private @Nullable @Column(unique = true) Long consoleChannelId;
    private @Nullable String consoleChannelPrefix;
    private @Nullable Long publicChannelEvents;
    private @Deprecated @Nullable Boolean fancyConsole = true;
}
