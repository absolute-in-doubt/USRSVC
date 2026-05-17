package com.innowise.userservice.application.dto;

public record UpdatePaymentCard(
        Long id,
        String cardNumber,
        String holder,
        String expirationDate,
        boolean active
) {
}
