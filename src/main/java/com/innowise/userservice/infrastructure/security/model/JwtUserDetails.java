package com.innowise.userservice.infrastructure.security.model;

public record JwtUserDetails(
        Long userId,
        String login
) {
}
