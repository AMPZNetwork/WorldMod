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
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.comroid.api.data.Vector;

import java.util.UUID;

import static com.ampznetwork.worldmod.api.game.Flag.*;

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

    //region Block Events
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockBreakEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Build);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockPlaceEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Build);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockBurnEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Fire_Damage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockCookEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Cook);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockDispenseEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Dispense);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockDispenseArmorEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Dispense);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockDropItemEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Drop);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockExplodeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Explode);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockFadeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Fade);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockFertilizeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Fertilize);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockFormEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Form);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockGrowEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Grow);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockIgniteEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Fire);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockShearEntityEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Shear);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockSpreadEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, Spread);
    }
    //endregion

    //region Player Events
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(AsyncPlayerChatEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Chat_Send);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerArmorStandManipulateEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Interact_ArmorStand);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerBedEnterEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Sleep);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerBucketEmptyEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Build, Interact);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerBucketFillEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Build, Interact);
    }

    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerCommandSendEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Chat_Command);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerDropItemEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Drop);
    }

    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerEggThrowEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Use_Egg);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerFishEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Interact_Fishing);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerHarvestBlockEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Build, Interact_Harvest);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerInteractEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Interact);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerInteractEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Interact);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerInteractAtEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Interact);
    }

    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerJoinEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Join);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerMoveEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Move);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerPickupArrowEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Pickup_Arrow);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerPortalEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Portal);
    }

    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerRespawnEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Respawn);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerShearEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Interact_Shear);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerTakeLecternBookEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Interact_Lectern);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerTeleportEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Move, Teleport);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerUnleashEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, Interact_Leash);
    }
    //endregion

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
