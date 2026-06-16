package net.hackyourfuture.tickettrackingsystem.exceptions;

// This fixes java:S112 by creating a distinct, explicit exception type
public class DatabaseAccessException extends RuntimeException {

    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}