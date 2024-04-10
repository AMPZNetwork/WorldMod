package org.comroid.mcsd.core.module.local;

import org.comroid.api.func.util.DelegateStream;
import org.comroid.mcsd.core.entity.module.local.LocalShellModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.module.ShellModule;

public class LocalShellModule extends ShellModule<LocalShellModulePrototype> {
    public LocalShellModule(Server server, LocalShellModulePrototype proto) {
        super(server, proto);
    }

    @Override
    public DelegateStream.IO execute(String... command) {
        return DelegateStream.IO.execute(command);
    }
}
