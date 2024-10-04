package com.ampznetwork.worldmod.spigot.adp.internal;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.core.event.EventDispatchBase;
import com.ampznetwork.worldmod.spigot.WorldMod$Spigot;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpellCastEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PigZombieAngerEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.StriderTemperatureChangeEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.comroid.api.data.Vector;

import static com.ampznetwork.worldmod.api.game.Flag.*;

@Value
public class SpigotEventDispatch extends EventDispatchBase implements Listener {
    WorldMod$Spigot worldMod;

    public SpigotEventDispatch(WorldMod$Spigot worldMod) {
        super(worldMod);
        this.worldMod = worldMod;
    }

    //region Block Events
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockBreakEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getBlock().getWorld().getName(), Build);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockPlaceEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getBlock().getWorld().getName(), Build);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockBurnEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Fire_Damage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockCookEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Cook);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockDispenseEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Dispense);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockDispenseArmorEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Dispense);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockDropItemEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getBlock().getWorld().getName(), Drop);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockExplodeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Explode);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockFadeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Fade);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockFertilizeEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getBlock().getWorld().getName(), Fertilize);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockFormEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Form);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockGrowEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Grow);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockIgniteEvent event) {
        var location = vec(event.getBlock().getLocation());
        var player = event.getPlayer();
        if (player == null)
            return;
        dispatchEvent(event, player.getUniqueId(), null, location, event.getBlock().getWorld().getName(), Fire);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockShearEntityEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Shear);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockSpreadEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event, null, null, location, event.getBlock().getWorld().getName(), Spread);
    }

    //region Player Events
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(AsyncPlayerChatEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Chat_Send);
    }
    //endregion

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerArmorStandManipulateEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact_ArmorStand);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerBedEnterEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Sleep);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerBucketEmptyEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Build);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerBucketFillEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Build);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerCommandSendEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Chat_Command);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerDropItemEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Drop);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerEggThrowEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Use_Egg);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerFishEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact_Fishing);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerHarvestBlockEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact_Harvest);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerInteractEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerInteractEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerInteractAtEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerJoinEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Join);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerMoveEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Move);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerPickupArrowEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Pickup_Arrow);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerPortalEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Portal);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerRespawnEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Respawn);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerShearEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact_Shear);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerTakeLecternBookEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact_Lectern);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerTeleportEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Teleport);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerUnleashEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getPlayer().getWorld().getName(), Interact_Leash);
    }

    //region Entity Events
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(AreaEffectCloudApplyEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Lingering_Apply);
    }
    //endregion

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(CreatureSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Spawn_Mobs);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(CreeperPowerEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Charge_Creeper);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(EntityAirChangeEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getName(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityBreakDoorEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), MobGriefing);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityBreedEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Breeding);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(EntityChangeBlockEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityCombustEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Combust);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityCombustByBlockEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Combust_ByBlock);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityCombustByEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Combust_ByEntity);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityDamageEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Damage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityDamageByBlockEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Damage_ByBlock);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityDamageByEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Damage_ByEntity);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityDropItemEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Drop);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityEnterBlockEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Hide);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityEnterLoveModeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Romance);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityExhaustionEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Exhaust);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityExplodeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Explode);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityInteractEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Interact);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityPickupItemEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Pickup);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityPortalEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Portal);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityRegainHealthEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Regenerate);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityResurrectEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Resurrect);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityShootBowEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(EntitySpawnEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getName(), location, Spawn);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntitySpellCastEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), SpellCast);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTameEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Tame);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTargetEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Target);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTargetLivingEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Target_Living);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTeleportEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Teleport);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityToggleGlideEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Glide);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityToggleSwimEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Swim);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTransformEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Transform);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(EntityUnleashEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getName(), location, Chat_Send);}
    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(ExpBottleEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getName(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ExplosionPrimeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Explode);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(FoodLevelChangeEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getName(), location, );}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(HorseJumpEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), HorseJump);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ItemDespawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Despawn);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ItemSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Spawn);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(LingeringPotionSplashEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Lingering);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PiglinBarterEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Barter);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PigZapEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Charge);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PigZombieAngerEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Anger);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerDeathEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getName(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerLeashEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getEntity().getWorld().getName(), Leash);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PotionSplashEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Splash);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ProjectileLaunchEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ProjectileHitEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(SheepDyeWoolEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getEntity().getWorld().getName(), Dye);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(SheepRegrowWoolEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Regrow);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(SlimeSplitEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), SlimeSplit);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(SpawnerSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Spawn);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(StriderTemperatureChangeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), TemperatureChange);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(VillagerAcquireTradeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Villager_Acquire);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(VillagerCareerChangeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Villager_Career);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(VillagerReplenishTradeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getName(), null, location, event.getEntity().getWorld().getName(), Villager_Replenish);
    }

    private void dispatchEvent(Cancellable cancellable, Object source, Object target, Vector.N3 location, String worldName, Flag flag) {
        dispatchEvent(new SpigotPropagationAdapter(cancellable), source, target, location, worldName, flag);
    }
    //endregion

    private static Vector.N3 vec(Location location) {
        return new Vector.N3(location.getX(), location.getY(), location.getZ());
    }
}
