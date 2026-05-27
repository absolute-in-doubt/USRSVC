package com.innowise.userservice.application.dto;

import java.util.List;

public record FullUserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean active,
        List<PaymentCardResponseDto> cards
) {
}
