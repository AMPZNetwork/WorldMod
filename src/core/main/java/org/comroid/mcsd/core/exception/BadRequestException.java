package org.comroid.mcsd.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends CommandStatusError {
    public BadRequestException(String message) {
        super(message);
    }

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
