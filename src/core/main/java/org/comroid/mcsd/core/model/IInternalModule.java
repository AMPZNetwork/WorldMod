package org.comroid.mcsd.core.model;

import org.comroid.mcsd.core.ServerManager;

public interface IInternalModule extends IServerModule {
    ServerManager.Entry getManagerEntry();
}
