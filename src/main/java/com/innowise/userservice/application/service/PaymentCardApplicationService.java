package com.innowise.userservice.application.service;

import com.innowise.userservice.application.dto.PageResponseDto;
import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardApplicationService {

    PaymentCardResponseDto getPaymentCardById(Long id) throws PaymentCardNotFoundException;

    PageResponseDto<PaymentCardResponseDto> getAllPaymentCards(PaymentCardFilter filter, Pageable pageable);

    PaymentCardResponseDto deactivateCardById(Long id) throws PaymentCardNotFoundException;

    PaymentCardResponseDto activateCardById(Long id) throws PaymentCardNotFoundException;

    List<PaymentCardResponseDto> getCardsByUserId(Long userId);

    void updateCard(UpdatePaymentCardDto updatePaymentCardDto, Long id) throws PaymentCardNotFoundException;
}
