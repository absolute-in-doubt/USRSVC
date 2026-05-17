package com.innowise.userservice.domain.port.in;

import com.innowise.userservice.application.dto.MessageResponseDto;
import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;


import java.util.List;

public interface PaymentCardController {

    ResponseEntity<PaymentCardResponseDto> getCardById(Long id) throws PaymentCardNotFoundException;

    ResponseEntity<Page<PaymentCardResponseDto>> getAllCards(PaymentCardFilter filter, Pageable pageable);

    ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(Long userId);

    ResponseEntity<MessageResponseDto> deactivateCardById(Long id) throws PaymentCardNotFoundException;

    ResponseEntity<MessageResponseDto> activateCardById(Long id) throws PaymentCardNotFoundException;

    ResponseEntity<MessageResponseDto> updateCard(UpdatePaymentCardDto updatePaymentCardDto, Long id)
            throws PaymentCardNotFoundException;
}
