package org.comroid.mcsd.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

public class StatusCode extends HttpStatusCodeException {
    public StatusCode(Throwable t) {
        this(t.getMessage());
    }

    public StatusCode(String reason) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, reason);
    }

    public StatusCode(HttpStatus status) {
        this(status, status.getReasonPhrase());
    }

    public StatusCode(HttpStatus status, String reason) {
        this(status.value(), reason);
    }

    public StatusCode(int code, String reason) {
        super(HttpStatusCode.valueOf(code), reason);
    }
}
