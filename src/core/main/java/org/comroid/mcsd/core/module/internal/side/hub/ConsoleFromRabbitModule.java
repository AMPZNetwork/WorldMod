package org.comroid.mcsd.core.module.internal.side.hub;

import org.comroid.api.net.Rabbit;
import org.comroid.mcsd.core.ServerManager;
import org.comroid.mcsd.api.dto.comm.ConsoleData;
import org.comroid.mcsd.core.entity.module.console.ConsoleModulePrototype;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static org.comroid.mcsd.api.dto.comm.ConsoleData.input;
import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

/** inits console bus with rabbitmq connection and sends commands through rabbitmq */
public class ConsoleFromRabbitModule extends ConsoleModule<@Nullable ConsoleModulePrototype> {
    public ConsoleFromRabbitModule(ServerManager.Entry entry) {
        super(Direction.Bidirectional, entry.getServer(), null);
    }

    @Override
    protected void $initialize() {
        var exchange = bean(Rabbit.class).bind("mcsd.module.console", "output."+server.getId(), ConsoleData.class);
        addChildren(
                exchange,

                // rabbit -> console
                bus = exchange
                        .filterData(cData -> cData.getType() == ConsoleData.Type.output)
                        .mapData(ConsoleData::getData)
        );
    }

    @Override
    public CompletableFuture<@Nullable String> execute(String input, @Nullable Pattern terminator) {
        return CompletableFuture.supplyAsync(() -> {
            bean(Rabbit.class).bind("mcsd.module.console","input."+server.getId(), ConsoleData.class)
                    .send(input(input));
            return null;
        });
    }
}
