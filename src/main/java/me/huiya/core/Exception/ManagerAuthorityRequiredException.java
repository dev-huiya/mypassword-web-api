package me.huiya.core.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class ManagerAuthorityRequiredException extends Exception {

    public ManagerAuthorityRequiredException(String message) { this(message, null); }

    public ManagerAuthorityRequiredException(String message, Throwable cause) { super(message, cause); }
}
