package com.innowise.userservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePaymentCardDto(
        @NotBlank @Size(min = 13, max = 19) String cardNumber,
        @NotBlank @Size(max = 100) String holder,
        @NotBlank LocalDate expirationDate,
        boolean active
) {
}
