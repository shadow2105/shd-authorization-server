package com.example.shdauthorizationserver.controller.exception;

class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
