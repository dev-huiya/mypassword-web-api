package me.huiya.core.Exception;

public class ParamRequiredException extends Exception {

    public ParamRequiredException(String message) { this(message, null); }

    public ParamRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
