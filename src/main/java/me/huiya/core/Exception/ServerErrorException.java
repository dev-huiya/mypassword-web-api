package me.huiya.core.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class ServerErrorException extends Exception {

    public ServerErrorException(String message) { this(message, null); }

    public ServerErrorException(String message, Throwable cause) { super(message, cause); }
}

