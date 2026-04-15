package com.example.recon.exception;

public class InvalidReportRequestException extends RuntimeException {
    public InvalidReportRequestException(String message) {
        super(message);
    }
}

