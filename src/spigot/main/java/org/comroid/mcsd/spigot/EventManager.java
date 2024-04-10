package org.comroid.mcsd.spigot;

import lombok.Value;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.comroid.api.tree.Component;
import org.comroid.mcsd.api.dto.comm.PlayerEvent;

@Value
public class EventManager extends Component.Base implements Listener {
    MCSD_Spigot plugin;

    @EventHandler
    public void handle(AsyncPlayerChatEvent event) {
        plugin.forward(new PlayerEvent(event.getPlayer().getName(), event.getMessage(), PlayerEvent.Type.Chat));
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        plugin.forward(new PlayerEvent(event.getPlayer().getName(), "Joined the game", PlayerEvent.Type.JoinLeave));
    }

    @EventHandler
    public void handle(PlayerKickEvent event) {
        plugin.forward(new PlayerEvent(event.getPlayer().getName(), "Left the game", PlayerEvent.Type.JoinLeave));
    }

    @EventHandler
    public void handle(PlayerDeathEvent event) {
        plugin.forward(new PlayerEvent(event.getEntity().getName(), event.getDeathMessage(), PlayerEvent.Type.Death));
    }

    @EventHandler
    public void handle(PlayerAdvancementDoneEvent event) {
        var adv = event.getAdvancement().getDisplay();
        plugin.forward(new PlayerEvent(event.getPlayer().getName(),
                //adv == null ?           todo: spigot fsr doesnt provide access to the advancement display class
                        "Has completed an advancement"
                ,//        : "Has completed the %s [%s]\n_%s_".formatted(adv.getType(), adv.getTitle(), adv.getDescription()),
                PlayerEvent.Type.Achievement));
    }
}
