package org.comroid.mcsd.core.module;

import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.mcsd.core.ServerManager;
import org.comroid.mcsd.core.entity.module.InternalModulePrototype;
import org.comroid.mcsd.core.model.IInternalModule;

@Value @NonFinal
public abstract class InternalModule extends ServerModule<InternalModulePrototype> implements IInternalModule {
    @ToString.Exclude ServerManager.Entry managerEntry;

    public InternalModule(ServerManager.Entry managerEntry) {
        super(managerEntry.getServer(), new InternalModulePrototype());
        this.managerEntry = managerEntry;
    }
}
