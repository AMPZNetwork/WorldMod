package com.ampznetwork.worldmod.api.internal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;
import org.comroid.api.tree.LifeCycle;

public interface EntityService extends LifeCycle {
    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
    enum DatabaseType implements Named {
        h2(org.h2.Driver.class),
        MySQL(com.mysql.jdbc.Driver.class);

        Class<?> driverClass;
    }
}
