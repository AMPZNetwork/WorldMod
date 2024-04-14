package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.WorldModCommands;
import com.ampznetwork.worldmod.api.event.EventDispatchBase;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Getter;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Command;
import org.comroid.api.info.Constraint;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import static com.ampznetwork.worldmod.api.game.Flag.Build;
import static org.bukkit.Bukkit.getPluginManager;

@Getter
public class WorldMod$Spigot extends JavaPlugin implements WorldMod {
    private final PlayerAdapter playerAdapter = new MyPlayerAdapter();
    private final EventDispatch eventDispatch = new EventDispatch();
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group> groups = new HashSet<>();
    private FileConfiguration config;
    private Command.Manager cmdr;

    private static Vector.N3 vec(Location location) {
        return new Vector.N3(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void onLoad() {
        this.config = getConfig();
        this.cmdr = new Command.Manager();
        cmdr.register(new WorldModCommands(this));
        cmdr.register(this);
        cmdr.new Adapter$Spigot(this);
        cmdr.initialize();
    }

    @Override
    public void onEnable() {
        getPluginManager().registerEvents(eventDispatch, this);
    }

    @Value
    private class MyPlayerAdapter implements PlayerAdapter {
        @Override
        public String getName(UUID playerId) {
            return getServer().getOfflinePlayer(playerId).getName();
        }

        @Override
        public boolean isOnline(UUID playerId) {
            return getServer().getPlayer(playerId) != null;
        }

        @Override
        public Vector.N3 getPosition(UUID playerId) {
            var player = getServer().getPlayer(playerId);
            Constraint.notNull(player, "player").run();
            var location = player.getLocation();
            return new Vector.N3(location.getX(), location.getY(), location.getZ());
        }
    }

    @Value
    private static class PropagationAdapter implements IPropagationAdapter {
        Cancellable cancellable;

        @Override
        public void cancel() {
            cancellable.setCancelled(true);
        }

        @Override
        public void force() {
            cancellable.setCancelled(false);
        }
    }

    @Value
    private class EventDispatch extends EventDispatchBase implements Listener {
        public EventDispatch() {
            super(WorldMod$Spigot.this);
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
            dispatchEvent(new PropagationAdapter(cancellable), playerId, location, flagChain);
        }
    }
}
