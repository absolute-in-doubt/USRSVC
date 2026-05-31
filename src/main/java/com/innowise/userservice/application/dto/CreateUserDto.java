package com.innowise.userservice.application.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateUserDto(
        @Positive Long userId,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Past LocalDate birthDate,
        @NotBlank @Email String email) {
}
