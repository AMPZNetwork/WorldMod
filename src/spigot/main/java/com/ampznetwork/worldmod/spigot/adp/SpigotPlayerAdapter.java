package com.ampznetwork.worldmod.spigot.adp;

import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.spigot.WorldMod$Spigot;
import lombok.Value;
import org.comroid.api.data.Vector;
import org.comroid.api.info.Constraint;

import java.util.UUID;

@Value
public class SpigotPlayerAdapter implements PlayerAdapter {
    WorldMod$Spigot worldMod;

    @Override
    public String getName(UUID playerId) {
        return worldMod.getServer().getOfflinePlayer(playerId).getName();
    }

    @Override
    public boolean isOnline(UUID playerId) {
        return worldMod.getServer().getPlayer(playerId) != null;
    }

    @Override
    public Vector.N3 getPosition(UUID playerId) {
        var player = worldMod.getServer().getPlayer(playerId);
        Constraint.notNull(player, "player").run();
        var location = player.getLocation();
        return new Vector.N3(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public String getWorldName(UUID playerId) {
        return worldMod.getServer().getPlayer(playerId).getWorld().getName();
    }
}
