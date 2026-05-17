package com.innowise.userservice.application.dto;

import java.time.LocalDate;

public record CreatePaymentCardDto(
        String cardNumber,
        String holder,
        LocalDate expirationDate
) {
    //active is automatically set to true when the payment card is created (in mapper)
}
