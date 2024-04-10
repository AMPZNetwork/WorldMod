package org.comroid.mcsd.core.exception;

import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.system.User;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InsufficientPermissionsException extends CommandStatusError {
    public InsufficientPermissionsException(User user, @Nullable Object related, AbstractEntity.Permission... missing) {
        super("User %s is missing permission %s for %s".formatted(user.getName(), Arrays.toString(missing), related));
    }

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
