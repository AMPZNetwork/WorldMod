package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.WorldModCommands;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Getter;
import lombok.Value;
import net.kyori.adventure.util.TriState;
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
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Constraint;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Stream;

import static com.ampznetwork.worldmod.api.game.Flag.Build;
import static com.ampznetwork.worldmod.api.game.Flag.Passthrough;
import static java.util.Comparator.comparingLong;
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

    private Stream<Region> findRegions(Vector.N3 location) {
        return regions.parallelStream()
                .filter(region -> region.streamChunks().anyMatch(chunk -> chunk.isInside(location)))
                .filter(region -> region.getShape().isPointInside(region.getSpatialAnchors(), location))
                .sorted(comparingLong(Prioritized::getPriority).reversed());
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
    private class EventDispatch implements Listener {
        private boolean dependsOnFlag(Cancellable cancellable, UUID playerId, Vector.N3 location, Flag... flagChain) {
            return dependsOnFlag(cancellable, playerId, location, Streams.OP.LogicalAnd, flagChain);
        }

        private boolean dependsOnFlag(Cancellable cancellable,
                                      UUID playerId,
                                      Vector.N3 location,
                                      @SuppressWarnings("SameParameterValue") Streams.OP chainOp_cancel,
                                      Flag... flagChain) {
            var iter = findRegions(location).iterator();
            var cancel = true;
            while (iter.hasNext()) {
                var region = iter.next();
                for (var flag : Arrays.stream(flagChain)
                        .flatMap(region::getFlagValues)
                        .toList()) {
                    if (!flag.appliesToUser(region, playerId))
                        continue;
                    var state = flag.getState();
                    if (state == TriState.NOT_SET)
                        continue;
                    if (state == TriState.FALSE)
                        cancel = chainOp_cancel.test(cancel, true);
                    else if (state == TriState.TRUE && flag.isForce())
                        cancel = chainOp_cancel.test(cancel, false);
                    cancellable.setCancelled(false);
                }
            }
            cancellable.setCancelled(cancel);
            return !cancel;
        }

        private boolean passthrough(Vector.N3 location) {
            return findRegions(location)
                    .map(region -> region.getFlagState(Passthrough))
                    .findFirst()
                    .filter(state -> state == TriState.TRUE)
                    .isPresent();
        }

        private void dispatchEvent(Cancellable cancellable, UUID playerId, Vector.N3 location, Flag... flagChain) {
            if (passthrough(location))
                return;
            dependsOnFlag(cancellable, playerId, location, flagChain);
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
    }
}
