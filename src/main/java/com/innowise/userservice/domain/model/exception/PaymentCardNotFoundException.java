package com.innowise.userservice.domain.model.exception;

public class PaymentCardNotFoundException extends Exception {
    public PaymentCardNotFoundException(Long cardId) {
        super("Failed to find a payment card with id: " + cardId);
    }
}
