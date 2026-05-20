package com.innowise.userservice.application.dto;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean active
) {
}
