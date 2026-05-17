package com.innowise.userservice.application.dto;

import jakarta.validation.constraints.Size;

public record PaymentCardFilter(
        @Size(max = 100) String userFirstName,
        @Size(max = 100) String userLastName
) {}
