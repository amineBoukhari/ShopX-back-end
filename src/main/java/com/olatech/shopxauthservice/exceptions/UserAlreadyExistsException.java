package com.olatech.shopxauthservice.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    private final String field;
    private final String message;

    public UserAlreadyExistsException(String field, String message) {
        super(message);
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
