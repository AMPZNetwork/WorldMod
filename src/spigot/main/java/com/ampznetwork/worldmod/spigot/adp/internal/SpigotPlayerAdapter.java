package com.ampznetwork.worldmod.spigot.adp.internal;

import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.spigot.WorldMod$Spigot;
import com.ampznetwork.worldmod.spigot.adp.game.SpigotBookAdapter;
import lombok.Value;
import net.kyori.adventure.text.EntityNBTComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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

    public void openBook(UUID playerId, EntityNBTComponent nbt) {
        var stack = new ItemStack(Material.WRITTEN_BOOK, 1);
        var meta = new SpigotBookAdapter();
        stack.setItemMeta(meta);
        Bukkit.getServer().getPlayer(playerId).openBook(stack);
    }
}
