package org.comroid.mcsd.core;

import lombok.Getter;
import org.comroid.mcsd.core.model.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
public class SidedConfiguration {
    private @Autowired @Getter ModuleType.Side side;
    private @Autowired CompletableFuture<?> hubConnect;

    public boolean isHubConnected() {
        return !hubConnect.isCompletedExceptionally();
    }
}
