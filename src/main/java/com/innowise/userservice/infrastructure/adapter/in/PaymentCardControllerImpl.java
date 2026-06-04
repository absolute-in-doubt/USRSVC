package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.service.PaymentCardApplicationService;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.domain.port.in.PaymentCardController;
import com.innowise.userservice.infrastructure.security.model.JwtUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentCardControllerImpl implements PaymentCardController {

    private final PaymentCardApplicationService service;

    @GetMapping("/cards/{paymentCardId}")
    @Secured({"USER","ADMIN"})
    public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable("paymentCardId") Long id,
            Authentication authentication) throws PaymentCardNotFoundException {
        PaymentCardResponseDto card = service.getPaymentCardById(id);
        JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();
        boolean hasAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!hasAdmin && !jwtUserDetails.userId().equals(card.userId())) {
            throw new AccessDeniedException("Users can only access their own cards");
        }
        return ResponseEntity.ok(card);
    }

    @GetMapping("/cards")
    @Secured({"ADMIN"})
    public ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getAllCards(@Valid @ParameterObject PaymentCardFilter filter,
                                                                               @ParameterObject @PageableDefault Pageable pageable){
        return ResponseEntity.ok(service.getAllPaymentCards(filter, pageable));
    }

    @GetMapping("/users/{userId}/cards")
    @Secured({"USER","ADMIN"})
    public ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(@PathVariable("userId") Long userId,
            Authentication authentication) {
        JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();
        boolean hasAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!hasAdmin && !jwtUserDetails.userId().equals(userId)) {
            throw new AccessDeniedException("Users can only access their own cards");
        }
        return ResponseEntity.ok(service.getCardsByUserId(userId));
    }

    @PatchMapping("/cards/{paymentCardId}/deactivate")
    @Secured({"USER","ADMIN"})
    public ResponseEntity<Void> deactivateCardById(@PathVariable("paymentCardId") Long id,
            Authentication authentication) throws PaymentCardNotFoundException {
        PaymentCardResponseDto card = service.getPaymentCardById(id);
        JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();
        boolean hasAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!hasAdmin && !jwtUserDetails.userId().equals(card.userId())) {
            throw new AccessDeniedException("Users can only access their own cards");
        }
        service.deactivateCardById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cards/{paymentCardId}/activate")
    @Secured({"USER","ADMIN"})
    public ResponseEntity<Void> activateCardById(@PathVariable("paymentCardId") Long id,
            Authentication authentication) throws PaymentCardNotFoundException {
        PaymentCardResponseDto card = service.getPaymentCardById(id);
        JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();
        boolean hasAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!hasAdmin && !jwtUserDetails.userId().equals(card.userId())) {
            throw new AccessDeniedException("Users can only access their own cards");
        }
        service.activateCardById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/cards/{paymentCardId}")
    @Secured({"USER","ADMIN"})
    public ResponseEntity<MessageResponseDto> updateCard(@Valid @RequestBody UpdatePaymentCardDto updatePaymentCardDto,
                                                        @PathVariable("paymentCardId") Long id,
                                                        Authentication authentication) throws PaymentCardNotFoundException {
        PaymentCardResponseDto card = service.getPaymentCardById(id);
        JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();
        boolean hasAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!hasAdmin && !jwtUserDetails.userId().equals(card.userId())) {
            throw new AccessDeniedException("Users can only access their own cards");
        }
        service.updateCard(updatePaymentCardDto, id);
        return ResponseEntity.ok(new MessageResponseDto("Card updated successfully"));
    }
}
