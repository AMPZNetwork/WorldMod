package com.ampznetwork.worldmod.spigot.adp;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.core.event.EventDispatchBase;
import com.ampznetwork.worldmod.spigot.WorldMod$Spigot;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
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
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getBlock().getWorld().getName(), Build);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockPlaceEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getBlock().getWorld().getName(), Build);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockBurnEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Fire_Damage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockCookEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Cook);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockDispenseEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Dispense);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockDispenseArmorEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Dispense);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockDropItemEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getBlock().getWorld().getName(), Drop);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockExplodeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Explode);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockFadeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Fade);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockFertilizeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getBlock().getWorld().getName(), Fertilize);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockFormEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Form);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockGrowEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Grow);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockIgniteEvent event) {
        var location = vec(event.getBlock().getLocation());
        var player = event.getPlayer();
        if (player == null)
            return;
        dispatchEvent(event, player.getUniqueId(), location, event.getBlock().getWorld().getName(), Fire);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockShearEntityEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Shear);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(BlockSpreadEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, location, event.getBlock().getWorld().getName(), Spread);
    }
    //endregion

    //region Player Events
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(AsyncPlayerChatEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Chat_Send);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerArmorStandManipulateEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Interact_ArmorStand);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerBedEnterEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Sleep);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerBucketEmptyEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Build, Interact);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerBucketFillEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Build, Interact);
    }

    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerCommandSendEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Chat_Command);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerDropItemEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Drop);
    }

    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerEggThrowEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Use_Egg);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerFishEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Interact_Fishing);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerHarvestBlockEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Build, Interact_Harvest);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerInteractEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Interact);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerInteractEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Interact);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerInteractAtEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Interact);
    }

    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerJoinEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Join);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerMoveEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Move);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerPickupArrowEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Pickup_Arrow);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerPortalEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Portal);
    }

    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerRespawnEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Respawn);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerShearEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Interact_Shear);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerTakeLecternBookEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Interact_Lectern);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerTeleportEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Move, Teleport);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerUnleashEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getPlayer().getWorld().getName(), Interact_Leash);
    }
    //endregion

    //region Entity Events
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(AreaEffectCloudApplyEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Lingering_Apply);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(CreatureSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Spawn_Mobs);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(CreeperPowerEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Charge_Creeper);
    }
    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(EntityAirChangeEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getUniqueId(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityBreakDoorEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), MobGriefing);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityBreedEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Breeding);
    }
    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(EntityChangeBlockEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityCombustEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Combust);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityCombustByBlockEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Combust_ByBlock);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityCombustByEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Combust_ByEntity);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityDamageEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Damage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityDamageByBlockEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Damage_ByBlock);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityDamageByEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Damage_ByEntity);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityDropItemEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Drop);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityEnterBlockEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Hide);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityEnterLoveModeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Romance);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityExhaustionEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Exhaust);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityExplodeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Explode);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityInteractEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Interact);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityPickupItemEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Pickup);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityPortalEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Portal);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityRegainHealthEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Regenerate);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityResurrectEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Resurrect);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityShootBowEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }
    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(EntitySpawnEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getUniqueId(), location, Spawn);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntitySpellCastEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), SpellCast);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityTameEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Tame);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityTargetEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Target);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityTargetLivingEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Target_Living);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityTeleportEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Teleport);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityToggleGlideEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Glide);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityToggleSwimEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Swim);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(EntityTransformEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Transform);
    }
    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(EntityUnleashEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getUniqueId(), location, Chat_Send);}
    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(ExpBottleEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getUniqueId(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(ExplosionPrimeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Explode);
    }
    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(FoodLevelChangeEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getUniqueId(), location, );}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(HorseJumpEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), HorseJump);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(ItemDespawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Despawn);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(ItemSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Spawn);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(LingeringPotionSplashEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Lingering);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PiglinBarterEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Barter);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PigZapEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Charge);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PigZombieAngerEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Anger);
    }
    //@EventHandler(priority = EventPriority.HIGHEST) public void dispatch(PlayerDeathEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getUniqueId(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PlayerLeashEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getEntity().getWorld().getName(), Leash);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(PotionSplashEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Splash);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(ProjectileLaunchEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(ProjectileHitEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(SheepDyeWoolEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), location, event.getEntity().getWorld().getName(), Dye);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(SheepRegrowWoolEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Regrow);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(SlimeSplitEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), SlimeSplit);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(SpawnerSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Spawn);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(StriderTemperatureChangeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), TemperatureChange);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(VillagerAcquireTradeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Villager_Acquire);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(VillagerCareerChangeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Villager_Career);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dispatch(VillagerReplenishTradeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getUniqueId(), location, event.getEntity().getWorld().getName(), Villager_Replenish);
    }
    //endregion

    private void dispatchEvent(Cancellable cancellable, UUID playerId, Vector.N3 location, String worldName, Flag... flagChain) {
        dispatchEvent(new SpigotPropagationAdapter(cancellable), playerId, location, worldName, flagChain);
    }
}
