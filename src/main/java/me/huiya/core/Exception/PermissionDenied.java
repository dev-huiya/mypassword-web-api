package me.huiya.core.Exception;

public class PermissionDenied extends Exception {

    public PermissionDenied(String message) { this(message, null); }

    public PermissionDenied(String message, Throwable cause) {
        super(message, cause);
    }
}