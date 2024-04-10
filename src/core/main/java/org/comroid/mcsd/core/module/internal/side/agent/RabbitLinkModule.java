package org.comroid.mcsd.core.module.internal.side.agent;

import lombok.extern.java.Log;
import org.comroid.api.net.Rabbit;
import org.comroid.mcsd.api.dto.comm.PlayerEvent;
import org.comroid.mcsd.core.ServerManager;
import org.comroid.mcsd.api.dto.comm.ConsoleData;
import org.comroid.mcsd.core.module.InternalModule;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.comroid.mcsd.core.module.player.PlayerEventModule;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static org.comroid.api.Polyfill.exceptionLogger;
import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

/** forwards console output to rabbitmq, and forward rabbitmq input to console */
@Log
public class RabbitLinkModule extends InternalModule {
    @Nullable
    @Inject(required = false)
    private ConsoleModule<?> console;
    @Nullable
    @Inject(required = false)
    private PlayerEventModule<?> players;

    public RabbitLinkModule(ServerManager.Entry entry) {
        super(entry);
    }

    @Override
    protected void $initialize() {
        final var rabbit = bean(Rabbit.class);
        if (console != null) {
            Rabbit.Exchange exchange = rabbit.exchange("mcsd.module.console");
            Rabbit.Exchange.Route<ConsoleData> route;
            addChildren(
                    // rabbit -> console
                    route = exchange.route("input." + server.getId(), ConsoleData.class),
                    route.filterData(cdat -> cdat.getType() == ConsoleData.Type.input)
                            .mapData(ConsoleData::getData)
                            .subscribeData(cmd -> console.execute(cmd).exceptionally(
                                    exceptionLogger(log, "Could not forward command '" + cmd + "' from Rabbit to Console"))),

                    // console -> rabbit
                    route = exchange.route("output." + server.getId(), ConsoleData.class),
                    console.getBus()
                            .filterData(str -> !str.isBlank())
                            //.filter(e -> DelegateStream.IO.EventKey_Output.equals(e.getKey()))
                            .mapData(str -> new ConsoleData(ConsoleData.Type.output, str))
                            .subscribeData(route::send)
            );
        }
        if (players != null) {
            // events -> rabbit
            Rabbit.Exchange exchange = rabbit.exchange("mcsd.module.player");
            Arrays.stream(PlayerEvent.Type.values()).forEach(type -> {
                Rabbit.Exchange.Route<PlayerEvent> route;
                addChildren(
                        route = exchange.route("event." + type.name().toLowerCase() + '.' + server.getId(), PlayerEvent.class),
                        players.getBus().filterData(e -> e.getType() == type).subscribeData(route::send)
                );
            });
        }
    }
}
