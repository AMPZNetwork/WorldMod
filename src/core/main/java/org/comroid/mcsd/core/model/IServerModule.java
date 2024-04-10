package org.comroid.mcsd.core.model;

import org.comroid.api.attr.Named;
import org.comroid.mcsd.core.entity.server.Server;

public interface IServerModule extends Named {
    Server getServer();
}
