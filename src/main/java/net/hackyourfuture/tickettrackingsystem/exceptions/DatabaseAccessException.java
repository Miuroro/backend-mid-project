package net.hackyourfuture.tickettrackingsystem.exceptions;


public class DatabaseAccessException extends RuntimeException {

    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}