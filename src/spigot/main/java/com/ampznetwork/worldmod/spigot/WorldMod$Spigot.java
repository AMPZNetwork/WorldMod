package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.WorldModCommands;
import com.ampznetwork.worldmod.api.model.Group;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.Region;
import lombok.Getter;
import lombok.Value;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Command;
import org.comroid.api.info.Constraint;

import java.util.*;

@Getter
public class WorldMod$Spigot extends JavaPlugin implements WorldMod {
    private final PlayerAdapter playerAdapter = new MyPlayerAdapter();
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group> groups = new HashSet<>();
    private FileConfiguration config;
    private Command.Manager cmdr;

    @Override
    public void onEnable() {
        this.config = getConfig();
        this.cmdr = new Command.Manager();
        cmdr.register(new WorldModCommands(this));
        cmdr.register(this);
        cmdr.new Adapter$Spigot(this);
        cmdr.initialize();
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
            return new Vector.N3(location.getX(),location.getY(),location.getZ());
        }
    }
}
