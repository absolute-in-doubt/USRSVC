package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.service.PaymentCardApplicationService;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.domain.port.in.PaymentCardController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentCardControllerImpl implements PaymentCardController {

    private final PaymentCardApplicationService service;

    @GetMapping("/cards/{paymentCardId}")
    public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable("paymentCardId") Long id) throws PaymentCardNotFoundException {
        return ResponseEntity.ok(service.getPaymentCardById(id));
    }

    @GetMapping("/cards")
    public ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getAllCards(@Valid @ParameterObject PaymentCardFilter filter,
                                                                               @ParameterObject @PageableDefault Pageable pageable){
        return ResponseEntity.ok(service.getAllPaymentCards(filter, pageable));
    }

    @GetMapping("/user/{userId}/cards")
    public ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.getCardsByUserId(userId));
    }

    @PatchMapping("/cards/{paymentCardId}/deactivate")
    public ResponseEntity<Void> deactivateCardById(@PathVariable("paymentCardId") Long id) throws PaymentCardNotFoundException {
        service.deactivateCardById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cards/{paymentCardId}/activate")
    public ResponseEntity<Void> activateCardById(@PathVariable("paymentCardId") Long id) throws PaymentCardNotFoundException {
        service.activateCardById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/cards/{paymentCardId}")
    public ResponseEntity<MessageResponseDto> updateCard(@Valid @RequestBody UpdatePaymentCardDto updatePaymentCardDto,
                                                        @PathVariable("paymentCardId") Long id) throws PaymentCardNotFoundException {
        service.updateCard(updatePaymentCardDto, id);
        return ResponseEntity.ok(new MessageResponseDto("Card updated successfully"));
    }
}
