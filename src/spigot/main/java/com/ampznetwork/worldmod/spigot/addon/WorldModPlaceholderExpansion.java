package com.ampznetwork.worldmod.spigot.addon;

import com.ampznetwork.libmod.api.util.ServerProperties;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.generated.PluginYml;
import lombok.EqualsAndHashCode;
import lombok.Value;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.comroid.api.data.bind.DataStructure;
import org.comroid.api.data.seri.type.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Value
@EqualsAndHashCode(callSuper = true)
public class WorldModPlaceholderExpansion extends PlaceholderExpansion {
    WorldMod mod;

    @Override
    public @NotNull String getIdentifier() {
        return PluginYml.name.toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return Arrays.toString(PluginYml.authors);
    }

    @Override
    public @NotNull String getVersion() {
        return PluginYml.version;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        final var prefix = "worldmod_";
        if (params.startsWith(prefix)) params = params.substring(prefix.length());

        var        i      = 0;
        Object     value  = player;
        var        path   = params.split("_");
        BlobSource source = RootSource.valueOf(path[i++]);
        while (source != null) {
            value  = source.apply(mod, value, path[i++]);
            source = value instanceof BlobSource bsrc ? bsrc : null;
            value  = value instanceof ObjectSource(Object target) ? target : value;
        }

        return String.valueOf(value);
    }

    private enum RootSource implements BlobSource {
        player {
            @Override
            public Object apply(WorldMod mod, Object source, String key) {
                return source instanceof OfflinePlayer plr ? new ObjectSource(mod.getPlayerAdapter()
                        .convertNativePlayer(plr)) : null;
            }
        }, region {
            @Override
            public Object apply(WorldMod mod, Object source, String key) {
                var playerAdapter = mod.getPlayerAdapter();
                return source instanceof OfflinePlayer plr ? new ObjectSource(mod.findRegions(playerAdapter.getPosition(
                        plr.getUniqueId()), playerAdapter.getWorldName(plr.getUniqueId()))) : null;
            }
        }, flag {
            @Override
            public Object apply(WorldMod mod, Object source, String key) {
                var playerAdapter = mod.getPlayerAdapter();
                var flag          = Flag.getForName(key);
                return source instanceof OfflinePlayer plr
                       ? mod.findRegions(playerAdapter.getPosition(plr.getUniqueId()),
                                playerAdapter.getWorldName(plr.getUniqueId()))
                               .map(rg -> rg.getEffectiveFlagValueForPlayer(flag,
                                       playerAdapter.convertNativePlayer(source).orElseThrow()))
                               .map(state -> (!"pvp".equals(flag.getName()) || !ServerProperties.LOCAL.isPvp()) || state.toBooleanOrElse(
                                       false))
                               .map(b -> b ? "yes" : "no")
                       : null;
            }
        }
    }

    private enum SecondarySource implements BlobSource {
        property {
            @Override
            public Object apply(WorldMod mod, Object source, String key) {
                return DataStructure.of(source.getClass()).getProperty(key);
            }
        }
    }

    private interface BlobSource {
        Object apply(WorldMod mod, Object source, String key);
    }

    private record ObjectSource(Object target) implements BlobSource {
        @Override
        public Object apply(WorldMod mod, Object $, String key) {
            var value = SecondarySource.property.apply(mod, target, key);
            return ValueType.of(value.getClass()).isStandard() ? value : new ObjectSource(value);
        }
    }
}
