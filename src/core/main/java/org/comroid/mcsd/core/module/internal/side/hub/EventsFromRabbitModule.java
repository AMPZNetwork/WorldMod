package org.comroid.mcsd.core.module.internal.side.hub;

import org.comroid.api.func.util.Event;
import org.comroid.api.net.Rabbit;
import org.comroid.mcsd.api.dto.comm.PlayerEvent;
import org.comroid.mcsd.core.ServerManager;
import org.comroid.mcsd.core.entity.module.player.PlayerEventModulePrototype;
import org.comroid.mcsd.core.module.player.PlayerEventModule;
import org.jetbrains.annotations.Nullable;

import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

public class EventsFromRabbitModule extends PlayerEventModule<@Nullable PlayerEventModulePrototype> {
    public EventsFromRabbitModule(ServerManager.Entry entry) {
        super(entry.getServer(), null);
    }

    @Override
    protected Event.Bus<PlayerEvent> initEventBus() {
        var binding = bean(Rabbit.class).bind("mcsd.module.player", "event.*."+server.getId(), PlayerEvent.class);
        addChildren(binding);
        return binding;
    }
}
