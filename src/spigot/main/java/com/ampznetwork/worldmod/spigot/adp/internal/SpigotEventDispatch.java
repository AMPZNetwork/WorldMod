package com.ampznetwork.worldmod.spigot.adp.internal;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.core.event.EventDispatchBase;
import com.ampznetwork.worldmod.spigot.WorldMod$Spigot;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Translatable;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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

import java.util.Optional;
import java.util.UUID;

import static com.ampznetwork.worldmod.api.game.Flag.*;

@Value
public class SpigotEventDispatch extends EventDispatchBase implements Listener {
    WorldMod$Spigot mod;

    public SpigotEventDispatch(WorldMod$Spigot mod) {
        super(mod);
        this.mod = mod;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockPlaceEvent event) {
        var block     = event.getBlock();
        var player    = event.getPlayer();
        var location  = vec(block.getLocation());
        var worldName = block.getWorld().getName();
        if (!tryDispatchWandEvent(event, worldName, player, location, block.getType()))
            dispatchEvent(event, player, block.getTranslationKey(), location, worldName, Build);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockBreakEvent event) {
        var block     = event.getBlock();
        var player    = event.getPlayer();
        var location  = vec(block.getLocation());
        var worldName = block.getWorld().getName();
        var itemInUse = player.getItemInUse();
        if (itemInUse == null || !tryDispatchWandEvent(event, worldName, player, location, itemInUse.getType()))
            dispatchEvent(event, player, block.getTranslationKey(), location, worldName, Build);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockBurnEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, null, block.getTranslationKey(), location, block.getWorld().getName(), Fire_Damage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockCookEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, block.getTranslationKey(), event.getSource().getTranslationKey(), location, block.getWorld().getName(), Cook);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockDispenseEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, block.getTranslationKey(), event.getItem().getTranslationKey(), location, block.getWorld().getName(), Dispense);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockDispenseArmorEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event,
                block.getTranslationKey(),
                tryGetAsPlayer(mod, event.getTargetEntity(), event.getItem().getTranslationKey()),
                location,
                block.getWorld().getName(),
                Dispense);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockDropItemEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, event.getPlayer(), null /*todo: too many items*/, location, block.getWorld().getName(), Drop);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockExplodeEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, block.getTranslationKey(), null, location, block.getWorld().getName(), Explode);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockFadeEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, block.getTranslationKey(), event.getNewState().getType().getTranslationKey(), location, block.getWorld().getName(), Fade);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockFertilizeEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, tryGetAsPlayer(mod, event.getPlayer()), block.getTranslationKey(), location, block.getWorld().getName(), Fertilize);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockFormEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, block.getTranslationKey(), event.getNewState().getType().getTranslationKey(), location, block.getWorld().getName(), Form);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockGrowEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, block.getTranslationKey(), event.getNewState().getType().getTranslationKey(), location, block.getWorld().getName(), Grow);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockIgniteEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event,
                Optional.<Object>ofNullable(tryGetAsPlayer(mod, event.getPlayer()))
                        .or(() -> Optional.<Translatable>ofNullable(event.getIgnitingBlock())
                                .or(() -> Optional.ofNullable(event.getIgnitingEntity())
                                        .map(Entity::getType))
                                .map(Translatable::getTranslationKey))
                        .orElse(event.getCause().name()),
                block.getTranslationKey(),
                location,
                block.getWorld().getName(),
                Fire);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockShearEntityEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event,
                event.getTool().getType().getTranslationKey(),
                event.getEntity().getType().getTranslationKey(),
                location,
                block.getWorld().getName(),
                Shear);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(BlockSpreadEvent event) {
        var block    = event.getBlock();
        var location = vec(block.getLocation());
        dispatchEvent(event, block.getTranslationKey(), event.getNewState().getType().getTranslationKey(), location, block.getWorld().getName(), Spread);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(AsyncPlayerChatEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer(), null, location, event.getPlayer().getWorld().getName(), Chat_Send);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerArmorStandManipulateEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer(), null/*todo*/, location, event.getPlayer().getWorld().getName(), Interact_ArmorStand);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerBedEnterEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer(), event.getBed().getTranslationKey(), location, event.getPlayer().getWorld().getName(), Sleep);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerBucketEmptyEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer(), event.getBucket().getTranslationKey(), location, event.getPlayer().getWorld().getName(), Build);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerBucketFillEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event,
                event.getPlayer(),
                event.getBlockClicked().getTranslationKey() /*todo: what about cauldrons etc*/,
                location,
                event.getPlayer().getWorld().getName(),
                Build);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerCommandSendEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Chat_Command);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerDropItemEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event,
                event.getPlayer(),
                event.getItemDrop().getItemStack().getTranslationKey(),
                location,
                event.getPlayer().getWorld().getName(),
                Drop);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerEggThrowEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Use_Egg);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerFishEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer(), event.getState().name(), location, event.getPlayer().getWorld().getName(), Interact_Fishing);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerHarvestBlockEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event,
                event.getPlayer(),
                event.getHarvestedBlock().getTranslationKey(),
                location,
                event.getPlayer().getWorld().getName(),
                Interact_Harvest);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerInteractEvent event) {
        var player    = event.getPlayer();
        var worldName = player.getWorld().getName();
        var location = vec(Optional.ofNullable(event.getClickedBlock())
                .map(Block::getLocation)
                .orElseGet(player::getEyeLocation));
        var itemInUse = event.getItem();
        if (itemInUse == null || !tryDispatchWandEvent(event, worldName, player, location, itemInUse.getType()))
            dispatchEvent(event,
                    player,
                    Optional.ofNullable(event.getClickedBlock()).map(Translatable::getTranslationKey).orElse(event.getAction().name()),
                    location,
                    worldName,
                    Interact);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerInteractEntityEvent event) {
        var player    = event.getPlayer();
        var worldName = player.getWorld().getName();
        var location  = vec(event.getRightClicked().getLocation());
        var itemInUse = player.getItemInUse();
        if (itemInUse == null || !tryDispatchWandEvent(event, worldName, player, location, itemInUse.getType()))
            dispatchEvent(event, player, event.getRightClicked().getType().getTranslationKey(), location, worldName, Interact);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerInteractAtEntityEvent event) {
        var player    = event.getPlayer();
        var worldName = player.getWorld().getName();
        var location  = vec(event.getRightClicked().getLocation());
        var itemInUse = player.getItemInUse();
        if (itemInUse == null || !tryDispatchWandEvent(event, worldName, player, location, itemInUse.getType()))
            dispatchEvent(event, player, event.getRightClicked().getType().getTranslationKey(), location, worldName, Interact);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerJoinEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Join);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerMoveEvent event) {
        if (event.getTo() == null) return; // nothing i can do
        var location = vec(event.getTo());
        dispatchEvent(event, event.getPlayer(), null, location, event.getPlayer().getWorld().getName(), Move);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerPickupArrowEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event,
                event.getPlayer(),
                event.getItem().getItemStack().getTranslationKey(),
                location,
                event.getPlayer().getWorld().getName(),
                Pickup_Arrow);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerPortalEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer(), event.getCause().name(), location, event.getPlayer().getWorld().getName(), Portal);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerRespawnEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Respawn);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerShearEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event,
                event.getPlayer(),
                event.getEntity().getType().getTranslationKey(),
                location,
                event.getPlayer().getWorld().getName(),
                Interact_Shear);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerTakeLecternBookEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event,
                event.getPlayer(),
                event.getLectern().getBlock().getTranslationKey(),
                location,
                event.getPlayer().getWorld().getName(),
                Interact_Lectern);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerTeleportEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer(), event.getCause().name(), location, event.getPlayer().getWorld().getName(), Teleport);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerUnleashEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer(), event.getEntityType().getTranslationKey(), location, event.getPlayer().getWorld().getName(), Interact_Leash);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(AreaEffectCloudApplyEvent event) {
        // todo???
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Lingering_Apply);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(CreatureSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Spawn_Mobs);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(CreeperPowerEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Charge_Creeper);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(EntityAirChangeEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getType().getTranslationKey(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityBreakDoorEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event,
                event.getEntity().getType().getTranslationKey(),
                event.getBlock().getTranslationKey(),
                location,
                event.getBlock().getWorld().getName(),
                MobGriefing);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityBreedEvent event) {
        var location = vec(event.getEntity().getLocation());
        var breeder  = event.getBreeder();
        dispatchEvent(event,
                event.getEntity().getType().getTranslationKey(),
                breeder == null ? null : breeder.getName(),
                location,
                event.getEntity().getWorld().getName(),
                Breeding);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(EntityChangeBlockEvent event) {var location = vec(event.getPlayer().getLocation());dispatchEvent(event, event.getPlayer().getUniqueId(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityCombustEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, null, event.getEntity().getType().getTranslationKey(), location, event.getEntity().getWorld().getName(), Combust);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityCombustByBlockEvent event) {
        var combuster = event.getCombuster();
        var location  = vec((combuster == null ? event.getEntity().getLocation() : combuster.getLocation()));
        dispatchEvent(event,
                combuster == null ? null : combuster.getTranslationKey(),
                event.getEntity().getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Combust_ByBlock);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityCombustByEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event,
                event.getCombuster().getType().getTranslationKey(),
                event.getEntity().getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Combust_ByEntity);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityDamageEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event,
                event.getCause().name(),
                event.getEntity().getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Damage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityDamageByBlockEvent event) {
        var damager  = event.getDamager();
        var location = vec((damager == null ? event.getEntity().getLocation() : damager.getLocation()));
        dispatchEvent(event,
                damager == null ? null : damager.getTranslationKey(),
                event.getEntity().getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Damage_ByBlock);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityDamageByEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event,
                event.getDamager().getType().getTranslationKey(),
                event.getEntity().getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Damage_ByEntity);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityDropItemEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), event.getItemDrop().getItemStack().getTranslationKey(), location,
                event.getEntity().getWorld().getName(), Drop);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityEnterBlockEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event,
                event.getEntity().getType().getTranslationKey(),
                event.getBlock().getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Hide);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityEnterLoveModeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Romance);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityExhaustionEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Exhaust);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityExplodeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Explode);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityInteractEvent event) {
        var location = vec(event.getBlock().getLocation());
        dispatchEvent(event,
                event.getEntity().getType().getTranslationKey(),
                event.getBlock().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Interact);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityPickupItemEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Pickup);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityPortalEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, null, event.getEntity().getType().getTranslationKey(), location, event.getEntity().getWorld().getName(), Portal);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityRegainHealthEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, null, event.getEntity().getType().getTranslationKey(), location, event.getEntity().getWorld().getName(), Regenerate);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityResurrectEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, null, event.getEntity().getType().getTranslationKey(), location, event.getEntity().getWorld().getName(), Resurrect);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityShootBowEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(EntitySpawnEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getType().getTranslationKey(), location, Spawn);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntitySpellCastEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), SpellCast);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTameEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event,
                event.getOwner().getUniqueId(),
                event.getEntity().getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Tame);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTargetEvent event) {
        var location = vec(event.getEntity().getLocation());
        var target   = event.getTarget();
        dispatchEvent(event,
                event.getEntity().getType().getTranslationKey(),
                target == null ? null : target.getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Target);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTargetLivingEntityEvent event) {
        var location = vec(event.getEntity().getLocation());
        var target   = event.getTarget();
        dispatchEvent(event,
                event.getEntity().getType().getTranslationKey(),
                target == null ? null : target.getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Target_Living);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTeleportEvent event) {
        if (event.getTo() == null) return; // nothing i can do
        var location = vec(event.getTo());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Teleport);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityToggleGlideEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Glide);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityToggleSwimEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Swim);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(EntityTransformEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Transform);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(EntityUnleashEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getType().getTranslationKey(), location, Chat_Send);}
    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(ExpBottleEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getType().getTranslationKey(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ExplosionPrimeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Explode);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(FoodLevelChangeEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getType().getTranslationKey(), location, );}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(HorseJumpEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), HorseJump);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ItemDespawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getItemStack().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(),
                Despawn);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ItemSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getItemStack().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Spawn);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(LingeringPotionSplashEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Lingering);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PiglinBarterEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event,
                event.getInput().getType().getTranslationKey(),
                event.getEntity().getType().getTranslationKey(),
                location,
                event.getEntity().getWorld().getName(),
                Barter);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PigZapEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Charge);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PigZombieAngerEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Anger);
    }

    //@EventHandler(priority = EventPriority.LOWEST) public void dispatch(PlayerDeathEvent event) {var location = vec(event.getEntity().getLocation());dispatchEvent(event, event.getEntity().getType().getTranslationKey(), location, Chat_Send);}
    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PlayerLeashEntityEvent event) {
        var location = vec(event.getPlayer().getLocation());
        dispatchEvent(event, event.getPlayer().getUniqueId(), null, location, event.getEntity().getWorld().getName(), Leash);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(PotionSplashEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Splash);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ProjectileLaunchEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(ProjectileHitEvent event) {
        var location = vec(Optional.ofNullable(event.getHitEntity())
                .map(Entity::getLocation)
                .or(() -> Optional.ofNullable(event.getHitBlock())
                        .map(Block::getLocation))
                .orElse(event.getEntity().getLocation()));
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Combat_Ranged);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(SheepRegrowWoolEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Regrow);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(SlimeSplitEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), SlimeSplit);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(SpawnerSpawnEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Spawn);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(StriderTemperatureChangeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), TemperatureChange);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(VillagerAcquireTradeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Villager_Acquire);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(VillagerCareerChangeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Villager_Career);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dispatch(VillagerReplenishTradeEvent event) {
        var location = vec(event.getEntity().getLocation());
        dispatchEvent(event, event.getEntity().getType().getTranslationKey(), null, location, event.getEntity().getWorld().getName(), Villager_Replenish);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean tryDispatchWandEvent(Cancellable event, String worldName, Player player, Vector.N3 location, Material wandMaterial) {
        return mod.findWandType(wandMaterial.getKey().toString())
                .filter(type -> tryDispatchWandEvent(new SpigotPropagationAdapter(event), worldName, tryGetAsPlayer(mod, player), location, type))
                .isPresent();
    }

    private void dispatchEvent(Cancellable cancellable, Object source, Object target, Vector.N3 location, String worldName, Flag flag) {
        dispatchEvent(new SpigotPropagationAdapter(cancellable), tryConvertPlayer(source), tryConvertPlayer(target), location, worldName, flag);
    }

    private Object tryConvertPlayer(Object object) {
        return object == null ? null : switch (object) {
            case org.bukkit.entity.Player player -> tryConvertPlayer(player.getUniqueId());
            case UUID id -> mod.getLib().getPlayerAdapter().getPlayer(id).orElseThrow();
            default -> object;
        };
    }

    private static Vector.N3 vec(Location location) {
        return new Vector.N3(location.getX(), location.getY(), location.getZ());
    }
}
