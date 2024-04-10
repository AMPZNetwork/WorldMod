package org.comroid.mcsd.core.module.remote.rcon;

import io.graversen.minecraft.rcon.MinecraftRcon;
import io.graversen.minecraft.rcon.service.ConnectOptions;
import io.graversen.minecraft.rcon.service.MinecraftRconService;
import io.graversen.minecraft.rcon.service.RconDetails;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.mcsd.core.entity.module.remote.rcon.RconModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class RconModule extends ConsoleModule<RconModulePrototype> {
    public RconModule(Server server, RconModulePrototype proto) {
        super(Direction.Input, server, proto);
    }

    private MinecraftRconService service;

    @Override
    protected void $initialize() {
        super.$initialize();

        var info = new RconDetails(
                server.getHost(),
                Objects.requireNonNullElse(proto.getPort(), RconModulePrototype.DefaultPort),
                proto.getPassword());
        var opts = new ConnectOptions(
                Integer.MAX_VALUE,
                Duration.ofHours(1),
                Duration.ofMinutes(30));
        service = new MinecraftRconService(info, opts);

        addChildren(bus.filter(e -> DelegateStream.IO.EventKey_Input.equals(e.getKey()))
                .subscribeData(this::execute));
    }

    @Override
    protected void $terminate() {
        service.disconnect();
        super.$terminate();
    }

    public CompletableFuture<String> execute(final String input) {
        return CompletableFuture.supplyAsync(() -> getClient().sendSync(() -> input))
                .thenApply(rconResponse -> {
                    var response = rconResponse.getResponseString();
                    bus.publish(DelegateStream.IO.EventKey_Output, response);
                    return response;
                });
    }

    @Override
    public CompletableFuture<String> execute(final String input, @Nullable Pattern $) {
        return execute(input);
    }

    private MinecraftRcon getClient() {
        if (!service.isConnected() && !service.connectBlocking(Duration.ofMinutes(1)))
            throw new RuntimeException("Could not connect RCon service");
        var client = service.minecraftRcon();
        if (client.isEmpty())
            throw new RuntimeException("Could not connect RCon service");
        return client.get();
    }
}
