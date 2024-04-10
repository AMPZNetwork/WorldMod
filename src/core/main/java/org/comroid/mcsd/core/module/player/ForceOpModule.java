package org.comroid.mcsd.core.module.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.comroid.api.tree.Component;
import org.comroid.mcsd.core.entity.module.console.ConsoleModulePrototype;
import org.comroid.mcsd.core.entity.module.player.ForceOpModulePrototype;
import org.comroid.mcsd.core.entity.module.player.PlayerEventModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.module.ServerModule;
import org.comroid.mcsd.core.module.console.ConsoleModule;

@Log
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForceOpModule extends ServerModule<ForceOpModulePrototype> {
    private @Inject ConsoleModule<ConsoleModulePrototype> console;
    private @Inject PlayerEventModule<PlayerEventModulePrototype> events;

    public ForceOpModule(Server server, ForceOpModulePrototype proto) {
        super(server, proto);
    }

    //todo
}
