package com.example.recon.exception;

public class FileNetConnectionException extends RuntimeException {
    public FileNetConnectionException(String message) {
        super(message);
    }

    public FileNetConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

