package org.comroid.mcsd.core.module;

import org.comroid.api.func.util.DelegateStream;
import org.comroid.mcsd.core.entity.module.ShellModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;

public abstract class ShellModule<T extends ShellModulePrototype> extends ServerModule<T> {
    public ShellModule(Server server, T proto) {
        super(server, proto);
    }

    public abstract DelegateStream.IO execute(String... command);
}
