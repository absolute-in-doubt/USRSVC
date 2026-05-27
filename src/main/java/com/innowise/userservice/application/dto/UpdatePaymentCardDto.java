package com.innowise.userservice.application.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdatePaymentCardDto(
        @NotBlank @Size(min = 13, max = 19) String cardNumber,
        @NotBlank @Size(max = 100) String holder,
        @FutureOrPresent LocalDate expirationDate,
        boolean active
) {
}
