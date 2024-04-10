package org.comroid.mcsd.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends CommandStatusError {
    public EntityNotFoundException(Class<?> type, Object id) {
        super("Entity of type %s with ID %s not found".formatted(type.getName(), id));
    }

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
