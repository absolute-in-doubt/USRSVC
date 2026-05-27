package com.innowise.userservice.domain.model.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(Long userId) {
        super("Failed to find user with id: " + userId);
    }
}
