package me.huiya.core.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Forbidden")
public class ForbiddenException extends Exception {

    public ForbiddenException(String message) {
        this(message, null);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

