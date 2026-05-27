package com.innowise.userservice.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePaymentCardDto(
        @NotBlank @Size(min = 16, max = 16) String cardNumber,
        @NotBlank @Size(max = 100) String holder,
        @Future LocalDate expirationDate
) {
}
