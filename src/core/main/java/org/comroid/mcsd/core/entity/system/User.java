package org.comroid.mcsd.core.entity.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;
import org.comroid.annotations.Category;
import org.comroid.annotations.Description;
import org.comroid.annotations.Ignore;
import org.comroid.api.func.ext.Wrap;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.func.util.Cache;
import org.comroid.api.info.Constraint;
import org.comroid.api.net.REST;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.module.discord.DiscordAdapter;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.logging.Level;

import static org.comroid.api.func.ext.Wrap.empty;
import static org.comroid.api.func.ext.Wrap.of;
import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

@Log
@Getter
@Setter
@Entity
@Category(value = "General", order = -99, desc = @Description("User Details"))
public class User extends AbstractEntity {
    private @Column(unique = true) @Nullable UUID hubId;
    private @Column(unique = true) @Nullable UUID minecraftId;
    private @Column(unique = true) @Nullable Long discordId;
    private @Column(unique = true) @Nullable String email;
    @Ignore
    @Nullable
    @ToString.Exclude
    @Column(unique = true)
    @Getter(onMethod = @__(@JsonIgnore))
    private String verification;
    @Ignore
    @Nullable
    @ToString.Exclude
    @Column(unique = true)
    @Getter(onMethod = @__(@JsonIgnore))
    private Instant verificationTimeout;

    @JsonIgnore
    public CompletableFuture<String> getMinecraftName() {
        Constraint.notNull(minecraftId, this + ".minecraftId").run();
        return Cache.get(minecraftId + ".username", () -> REST.get(getMojangAccountUrl(minecraftId))
                .thenApply(REST.Response::validate2xxOK)
                .thenApply(rsp -> rsp.getBody().get("name").asString())
                .exceptionally(t -> {
                    log.log(Level.WARNING, "Could not retrieve Minecraft Username for user " + minecraftId, t);
                    return "Steve";
                }));
    }

    @JsonIgnore
    public CompletableFuture<DisplayUser> getDiscordDisplayUser() {
        Constraint.notNull(discordId, this+".discordId").run();
        assert discordId != null;
        return bean(DiscordAdapter.class).getJda()
                .retrieveUserById(discordId)
                .map(usr->new DisplayUser(DisplayUser.Type.Discord,usr.getEffectiveName(),usr.getEffectiveAvatarUrl(),null))
                .submit()
        /*
        return REST.request(REST.Method.GET, getDiscordUserUrl(discordId))
                .addHeader("Authorization", "Bot "+bean(McsdConfig.class).getDiscordToken())
                .execute()
                .thenApply(REST.Response::validate2xxOK)
                .thenApply(rsp -> {
                    rsp.require(200, "Invalid response");
                    return rsp.getBody();
                })
                .thenApply(data->new DisplayUser(DisplayUser.Type.Discord,
                        data.get("username").asString(),
                        getDiscordAvatarUrl(discordId, data.get("avatar").asString()),
                        null))
         */
                .exceptionally(t-> {
                    log.log(Level.WARNING, "Could not retrieve Discord User data for user " + discordId, t);
                    return new DisplayUser(DisplayUser.Type.Discord,
                            getName(),
                            "https://cdn.discordapp.com/embed/avatars/0.png",
                            null);
                });
    }

    public String blockingMinecraftName() {
        return getMinecraftName().join();
    }

    @Override
    public String toString() {
        return "User " + getName();
    }

    public String getNameMcURL() {return minecraftId==null
            ?"https://github.com/comroid-git/mc-server-hub/blob/main/docs/account_not_linked.md"
            :"https://namemc.com/profile/" + minecraftId;}
    public String getHeadURL() {return minecraftId==null
            ?"https://github.com/comroid-git/mc-server-hub/blob/main/docs/account_not_linked.md"
            :"https://mc-heads.net/avatar/" + minecraftId;}
    public String getIsoBodyURL() {return minecraftId==null
            ?"https://github.com/comroid-git/mc-server-hub/blob/main/docs/account_not_linked.md"
            :"https://mc-heads.net/body/" + minecraftId;}

    public Wrap<DisplayUser> getDisplayUser(DisplayUser.Type... types) {
        Constraint.Length.min(1, types, "types").run();
        Wrap<DisplayUser> result = null;
        DisplayUser.Type type = DisplayUser.Type.Hub;
        int i = -1;
        do {
            try {
                do {
                    if (i + 1 >= types.length)
                        result = empty();
                    type = types[++i];
                } while (!type.test(this));
                result = type == DisplayUser.Type.Discord
                        ? of(getDiscordDisplayUser().join())
                        : of(new DisplayUser(type,
                        type == DisplayUser.Type.Minecraft
                                ? getMinecraftName().join()
                                : getName(),
                        getHeadURL(),
                        getNameMcURL()));
            } catch (Throwable t) {
                log.log(Level.WARNING, "Could not retrieve " + type + " display for for user " + getId(), t);
            }
        } while (result == null || result.isNull());
        return result;
    }

    public static String getMojangAccountUrl(String username) {
        return "https://api.mojang.com/users/profiles/minecraft/" + username;
    }

    public static String getMojangAccountUrl(UUID id) {
        return "https://sessionserver.mojang.com/session/minecraft/profile/" + id;
    }

    public static String getDiscordUserUrl(long id) {
        return "https://discord.com/api/users/"+id;
    }

    public static String getDiscordAvatarUrl(long id, String hash) {
        return "https://cdn.discordapp.com/avatars/"+id+"/"+hash+".png";
    }

    public boolean canModify(AbstractEntity target) {
        return hasPermission(this, Permission.Modify) && (!(target instanceof User usr) || canGovern(usr));
    }

    public boolean canAdmin(AbstractEntity target) {
        return hasPermission(this, Permission.Administrate) && (!(target instanceof User usr) || canGovern(usr));
    }

    public boolean canGovern(User target) {
        return this.getId().equals(target.getId()) || hasPermission(this, Permission.ManageUsers);
    }

    public record DisplayUser(Type type, String username, String avatarUrl, @Nullable String url) {
        public enum Type implements Predicate<User> {
            Minecraft {
                @Override
                public boolean test(User user) {
                    return user.minecraftId != null;
                }
            },
            Discord {
                @Override
                public boolean test(User user) {
                    return user.discordId != null;
                }
            },
            Hub {
                @Override
                public boolean test(User user) {
                    return user.hubId != null;
                }
            };

            @Override
            public abstract boolean test(User user);
        }
    }

    @Deprecated
    public enum Perm implements Bitmask.Attribute<Perm> {None, ManageServers, ManageShConnections, Admin;}
}
