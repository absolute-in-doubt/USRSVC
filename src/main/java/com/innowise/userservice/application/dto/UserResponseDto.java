package com.innowise.userservice.application.dto;

public record UserResponseDto(
        Long id,
        String name,
        String surname,
        String email,
        boolean active
) {
}
