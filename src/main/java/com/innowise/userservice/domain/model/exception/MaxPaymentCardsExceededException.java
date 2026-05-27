package com.innowise.userservice.domain.model.exception;

public class MaxPaymentCardsExceededException extends Exception {
  public MaxPaymentCardsExceededException(Long userId) {
    super("Max payment cards limit exceeded for user with id: " + userId);
  }
}
