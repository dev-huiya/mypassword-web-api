package me.huiya.core.Exception;

public class ParamMismatchException extends Exception {

    public ParamMismatchException(String message) { this(message, null); }

    public ParamMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
