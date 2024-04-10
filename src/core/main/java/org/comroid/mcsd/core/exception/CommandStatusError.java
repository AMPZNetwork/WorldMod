package org.comroid.mcsd.core.exception;

import org.comroid.api.func.util.Command;
import org.springframework.http.HttpStatus;

public abstract class CommandStatusError extends Command.Error {
    public CommandStatusError(String message) {
        super(message);
    }

    protected abstract HttpStatus getStatus();

    public StatusCode toStatusCodeExc() {
        return new StatusCode(getStatus(), getMessage());
    }
}
