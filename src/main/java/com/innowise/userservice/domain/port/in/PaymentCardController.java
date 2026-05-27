package com.innowise.userservice.domain.port.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface PaymentCardController {

    ResponseEntity<PaymentCardResponseDto> getCardById(Long id) throws PaymentCardNotFoundException;

    ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getAllCards(@Valid PaymentCardFilter filter, Pageable pageable);

    ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(Long userId);

    ResponseEntity<Void> deactivateCardById(Long id) throws PaymentCardNotFoundException;

    ResponseEntity<Void> activateCardById(Long id) throws PaymentCardNotFoundException;

    ResponseEntity<MessageResponseDto> updateCard(@Valid UpdatePaymentCardDto updatePaymentCardDto, Long id)
            throws PaymentCardNotFoundException;
}
