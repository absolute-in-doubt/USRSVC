package com.innowise.userservice.application.dto;

import jakarta.validation.constraints.Size;

public record UserFilter(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName
) {}
