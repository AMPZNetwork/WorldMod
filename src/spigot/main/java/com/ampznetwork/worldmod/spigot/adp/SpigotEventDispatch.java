package com.ampznetwork.worldmod.spigot.adp;

import com.ampznetwork.worldmod.api.event.EventDispatchBase;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.spigot.WorldMod$Spigot;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.comroid.api.data.Vector;

import java.util.UUID;

import static com.ampznetwork.worldmod.api.game.Flag.Build;

@Value
public class SpigotEventDispatch extends EventDispatchBase implements Listener {
    WorldMod$Spigot worldMod$Spigot;

    public SpigotEventDispatch(WorldMod$Spigot worldMod$Spigot) {
        super(worldMod$Spigot);
        this.worldMod$Spigot = worldMod$Spigot;
    }

    private static Vector.N3 vec(Location location) {
        return new Vector.N3(location.getX(), location.getY(), location.getZ());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Build);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Build);
    }

    //region todo
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
    }
    //endregion

    private void dispatchEvent(Cancellable cancellable, UUID playerId, Vector.N3 location, Flag... flagChain) {
        dispatchEvent(new SpigotPropagationAdapter(cancellable), playerId, location, flagChain);
    }
}
