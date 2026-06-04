package com.innowise.userservice.application.service;

import com.innowise.userservice.application.dto.PageResponseDto;
import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardApplicationService {

    PaymentCardResponseDto getPaymentCardById(Long id, Long authenticatedUserId, boolean isAdmin) throws PaymentCardNotFoundException;

    PageResponseDto<PaymentCardResponseDto> getAllPaymentCards(PaymentCardFilter filter, Pageable pageable);

    PaymentCardResponseDto deactivateCardById(Long id, Long authenticatedUserId, boolean isAdmin) throws PaymentCardNotFoundException;

    PaymentCardResponseDto activateCardById(Long id, Long authenticatedUserId, boolean isAdmin) throws PaymentCardNotFoundException;

    List<PaymentCardResponseDto> getCardsByUserId(Long userId, Long authenticatedUserId, boolean isAdmin);

    void updateCard(UpdatePaymentCardDto updatePaymentCardDto, Long id, Long authenticatedUserId, boolean isAdmin) throws PaymentCardNotFoundException;
}
