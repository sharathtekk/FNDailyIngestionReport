package com.example.recon.exception;

public class ReportWriteException extends RuntimeException {
    public ReportWriteException(String message) {
        super(message);
    }

    public ReportWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}

