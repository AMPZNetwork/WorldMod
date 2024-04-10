package org.comroid.mcsd.core.repo.system;

import org.comroid.api.func.ext.Wrap;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.info.Log;
import org.comroid.api.net.Token;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.system.AuthorizationLink;
import org.comroid.mcsd.core.entity.system.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;

public interface AuthorizationLinkRepo extends CrudRepository<AuthorizationLink, String> {
    default AuthorizationLink create(User user, UUID targetId, AbstractEntity.Permission... permissions) {
        return create(user,targetId, Bitmask.combine(permissions));
    }
    default AuthorizationLink create(User user, UUID targetId, long permissions) {
        String code;
        do {
            code = Token.random(16, false);
        } while (findById(code).isPresent());
        var link = new AuthorizationLink(code, user, targetId, permissions);
        return save(link);
    }

    default Wrap<AuthorizationLink> validate(User user, UUID target, String code, AbstractEntity.Permission... permissions) {
        return validate(user, target, code, Bitmask.combine(permissions));
    }
    default Wrap<AuthorizationLink> validate(User user, UUID target, String code, long permissions) {
        return Wrap.ofSupplier(()-> Optional.ofNullable(code)
                .flatMap(this::findById)
                .filter(link -> link.getCreator().equals(user))
                .filter(link -> link.getTarget().equals(target))
                .filter(link -> link.getPermissions() == permissions)
                .orElse(null));
    }

    default void flush(String... codes) {
        try {
            deleteAllById(Arrays.stream(codes)
                    .filter(Objects::nonNull)
                    .filter(Predicate.not(String::isBlank))
                    .toList());
        } catch (Throwable t) {
            Log.at(Level.FINE, "Error flushing Authorizations", t);
        }
    }
}
