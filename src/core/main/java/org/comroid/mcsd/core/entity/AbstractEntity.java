package org.comroid.mcsd.core.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.comroid.annotations.*;
import org.comroid.api.attr.Named;
import org.comroid.api.func.ext.Wrap;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.info.Maintenance;
import org.comroid.api.text.Capitalization;
import org.comroid.mcsd.core.entity.system.User;
import org.comroid.mcsd.core.exception.InsufficientPermissionsException;
import org.comroid.mcsd.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Data
@Slf4j
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Category(value = "Entity", order = Integer.MIN_VALUE, desc = @Description("Basic Entity related Information"))
public abstract class AbstractEntity implements Named {
    public static final Maintenance.Inspection MI_InsufficientPermission = Maintenance.Inspection.builder()
            .name("InsufficientPermission")
            .description("User has insufficient permission")
            .description("User %s is missing required permission %s")
            .build();
    public static final int CurrentVersion = 1;
    @Id @Readonly @Order(Integer.MIN_VALUE)
    private UUID id = UUID.randomUUID();
    @Setter
    @Nullable
    @Order(Integer.MIN_VALUE+1)
    private String name;
    @Setter
    @Nullable
    @Order(Integer.MIN_VALUE+2)
    private String displayName;
    @Setter
    @Nullable
    @ManyToOne
    private User owner;
    @Readonly
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<User, @NotNull Long> permissions;
    private @Ignore @Nullable Integer version = CurrentVersion;

    public String getBestName() {
        return Optional.ofNullable(displayName)
                .or(() -> Optional.ofNullable(name))
                .or(() -> Optional.ofNullable(owner)
                        .map(AbstractEntity::getBestName)
                        .map(n -> n + "s " + getClass().getSimpleName()))
                .filter(Predicate.not("null"::equals))
                .orElseGet(id::toString);
    }

    public String getTypeName() {
        return Capitalization.Title_Case.convert(getClass().getSimpleName());
    }

    public boolean isUser() {
        return owner == null;
    }

    public Stream<? extends AbstractEntity> managedChildren() {
        return Stream.empty();
    }

    @Deprecated
    public final boolean hasAnyPermission(@NotNull User user) {
        return hasPermission(user, Permission.Any);
    }

    public boolean hasPermission(@NotNull User user, long anyOf) {
        return hasPermission(user, Bitmask.Attribute
                .valueOf(anyOf, Permission.class)
                .toArray(Permission[]::new));
    }

    public boolean hasPermission(@NotNull User user, AbstractEntity.Permission... permissions) {
        Permission[] perms;
        if(permissions.length==0)
            perms=new Permission[]{Permission.Any};
        else perms = permissions;
        final var mask = this.permissions.getOrDefault(user, 0L);
        return (owner != null && user.getId().equals(owner.getId()))
                || Arrays.stream(perms).allMatch(flag -> Bitmask.isFlagSet(mask, flag))
                || Utils.SuperAdmins.contains(user.getId())
                || managedChildren().anyMatch(e->e.hasPermission(user,permissions));
    }

    public final Wrap<AbstractEntity> verifyPermission(final @NotNull User user, final AbstractEntity.Permission... permissions) {
        return () -> hasPermission(user, permissions) ? this : null;
    }

    public final void requirePermission(final @NotNull User user, final AbstractEntity.Permission... permissions) {
        verifyPermission(user, permissions).orElseThrow(()->new InsufficientPermissionsException(user,this,permissions));
    }

    @Override
    public String getAlternateName() {
        return Optional.ofNullable(getDisplayName()).orElseGet(this::getName);
    }

    public String toString() {
        return getClass().getSimpleName() + ' ' + getBestName();
    }

    public final boolean equals(Object other) {
        return other instanceof AbstractEntity && id.equals(((AbstractEntity) other).id);
    }

    public final int hashCode() {
        return id.hashCode();
    }

    // todo: ungroup permissions?
    public enum Permission implements Bitmask.Attribute<Permission> {
        None(0),
        Status,
        Whitelist,
        Kick,
        Mute,
        Ban,
        Start,
        Stop,
        Backup,
        Update,
        SwitchMaintenance,
        Enable,
        Console,
        Execute,
        Files,
        ForceOP,
        TriggerCron,

        CreateAgent,
        CreateDiscordBot,
        CreateServer,
        CreateSh,
        Modify,
        Refresh,
        Reload,
        ManageModules,
        ManageUsers(0x2000_0000),

        View(0x0100_0000_0000_0000L, Status),
        Moderate(0x0200_0000_0000_0000L, Whitelist, Kick, Mute),
        Manage(0x0400_0000_0000_0000L, Ban, Start, Stop, Backup, Update, SwitchMaintenance, Enable),
        Administrate(0x0800_0000_0000_0000L, Console, Execute, Files, ForceOP, TriggerCron),
        Delete(0x1000_0000_0000_0000L),

        @Deprecated
        Any(0xffff_ffffL);

        static {
            log.info("Registered permissions up to "+Delete);
        }

        private final long value;

        Permission() {
            this(Bitmask.nextFlag());
        }

        Permission(long base, Permission... members) {this.value = base | Bitmask.combine(members);}

        @Override
        public @NotNull Long getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "%s(0x%x)".formatted(name(),value);
        }
    }

    public interface Repo<T extends AbstractEntity> extends CrudRepository<T, UUID> {
        @Query("SELECT e FROM #{#entityName} e WHERE e.version = null OR e.version <= :version")
        Iterable<T> findMigrationCandidates(@Param("version") int fromVersion);
    }
}
