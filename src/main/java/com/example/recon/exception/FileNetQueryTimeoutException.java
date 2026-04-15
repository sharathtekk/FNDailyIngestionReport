package com.example.recon.exception;

public class FileNetQueryTimeoutException extends RuntimeException {
    public FileNetQueryTimeoutException(String message) {
        super(message);
    }

    public FileNetQueryTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

