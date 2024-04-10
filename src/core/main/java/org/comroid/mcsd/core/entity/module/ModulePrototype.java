package org.comroid.mcsd.core.entity.module;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.comroid.annotations.Readonly;
import org.comroid.api.Polyfill;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.entity.system.User;
import org.comroid.mcsd.core.model.ModuleType;
import org.comroid.mcsd.core.module.ServerModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static org.comroid.api.java.StackTraceUtils.lessSimpleName;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("JpaDataSourceORMInspection") // false positive
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"server_id", "dtype"}))
public abstract class ModulePrototype extends AbstractEntity {
    private @ManyToOne Server server;
    private @Readonly @Convert(converter = ModuleType.Converter.class) ModuleType<?, ?> dtype = ModuleType.of(this).assertion("Unsupported type: " + lessSimpleName(getClass()));
    private boolean enabled = true;

    @Override
    public @Nullable String getDisplayName() {
        return Objects.requireNonNullElseGet(super.getDisplayName(), () -> dtype + " for " + server);
    }

    public <T extends ServerModule<P>, P extends ModulePrototype> T toModule(Server server) {
        if (!dtype.getProto().getType().isAssignableFrom(getClass()))
            throw new RuntimeException("Invalid dtype " + dtype + " for module " + this);
        var module = dtype.getCtor().autoInvoke(server, this);
        return Polyfill.uncheckedCast(module);
    }

    @Override
    public boolean hasPermission(@NotNull User user, Permission... permissions) {
        return super.hasPermission(user, permissions) || server.hasPermission(user, permissions);
    }
}
