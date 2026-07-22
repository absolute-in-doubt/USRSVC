package com.innowise.userservice.application.dto;

public record PaymentCardResponseDto(
        Long id,
        Long userId,
        String cardNumber,
        String holder,
        String expirationDate,
        boolean active
) {
}
