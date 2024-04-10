package org.comroid.mcsd.core.module.status;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.comroid.api.os.OS;
import org.comroid.api.tree.Component;
import org.comroid.mcsd.core.entity.module.status.UptimeModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.entity.server.ServerUptimeEntry;
import org.comroid.mcsd.core.module.ServerModule;
import org.comroid.mcsd.core.module.local.LocalExecutionModule;
import org.comroid.mcsd.core.repo.server.ServerUptimeRepo;

import java.util.concurrent.CompletableFuture;

import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

@Log
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UptimeModule extends ServerModule<UptimeModulePrototype> {
    private @Inject LocalExecutionModule execution;
    private @Inject StatusModule statusModule;

    public UptimeModule(Server server, UptimeModulePrototype proto) {
        super(server, proto);
    }

    @Override
    protected void $tick() {
        pushUptime();
    }

    public CompletableFuture<Void> pushUptime() {
        return server.status()
                .thenCombine(execution.getProcess()!=null&&execution.getProcess().isAlive()
                                ? OS.current.getRamUsage(execution.getProcess().pid())
                                : CompletableFuture.completedFuture(0L),
                        (stat, ram) -> new ServerUptimeEntry(server,
                                statusModule.getCurrentStatus().getStatus(),
                                stat.getPlayers() != null ? stat.getPlayers().size() : stat.getPlayerCount(),
                                ram))
                .thenAccept(bean(ServerUptimeRepo.class)::save);
    }
}
