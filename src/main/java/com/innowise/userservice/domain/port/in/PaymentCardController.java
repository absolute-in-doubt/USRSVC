package com.innowise.userservice.domain.port.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.domain.model.exception.AccessDeniedException;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.infrastructure.security.model.JwtUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface PaymentCardController {

    ResponseEntity<PaymentCardResponseDto> getCardById(Long id,
            JwtUserDetails jwtUserDetails,
            Authentication authentication) throws PaymentCardNotFoundException, AccessDeniedException;

    ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getAllCards(@Valid PaymentCardFilter filter, Pageable pageable);

    ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(Long userId,
            JwtUserDetails jwtUserDetails,
            Authentication authentication) throws AccessDeniedException;

    ResponseEntity<Void> deactivateCardById(Long id,
            JwtUserDetails jwtUserDetails,
            Authentication authentication) throws PaymentCardNotFoundException, AccessDeniedException;

    ResponseEntity<Void> activateCardById(Long id,
            JwtUserDetails jwtUserDetails,
            Authentication authentication) throws PaymentCardNotFoundException, AccessDeniedException;

    ResponseEntity<MessageResponseDto> updateCard(@Valid UpdatePaymentCardDto updatePaymentCardDto, Long id,
            JwtUserDetails jwtUserDetails,
            Authentication authentication) throws PaymentCardNotFoundException, AccessDeniedException;
}
