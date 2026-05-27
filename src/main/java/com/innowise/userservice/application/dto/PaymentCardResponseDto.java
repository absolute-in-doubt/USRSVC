package com.innowise.userservice.application.dto;

public record PaymentCardResponseDto(
        Long id,
        String cardNumber,
        String holder,
        String expirationDate,
        boolean active
) {
}
