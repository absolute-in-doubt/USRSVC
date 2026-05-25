package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.application.dto.MessageResponseDto;
import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.application.service.PaymentCardApplicationService;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.domain.port.in.PaymentCardController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponseDto>> getAllCards(@Valid @ParameterObject PaymentCardFilter filter,
                                                                    @ParameterObject @PageableDefault Pageable pageable){
        return ResponseEntity.ok(service.getAllPaymentCards(filter, pageable));
    }

    @GetMapping("/user/{userId}/cards")
    public ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.getCardsByUserId(userId));
    }

    @PatchMapping("/cards/{paymentCardId}/deactivate")
    public ResponseEntity<MessageResponseDto> deactivateCardById(@PathVariable("paymentCardId") Long id) throws PaymentCardNotFoundException {
        service.deactivateCardById(id);
        return ResponseEntity.ok(new MessageResponseDto("Card deactivated successfully"));
    }

    @PatchMapping("/cards/{paymentCardId}/activate")
    public ResponseEntity<MessageResponseDto> activateCardById(@PathVariable("paymentCardId") Long id) throws PaymentCardNotFoundException {
        service.activateCardById(id);
        return ResponseEntity.ok(new MessageResponseDto("Card activated successfully"));
    }

    @PutMapping("/cards/{paymentCardId}")
    public ResponseEntity<MessageResponseDto> updateCard(@Valid @RequestBody UpdatePaymentCardDto updatePaymentCardDto,
                                                        @PathVariable("paymentCardId") Long id) throws PaymentCardNotFoundException {
        service.updateCard(updatePaymentCardDto, id);
        return ResponseEntity.ok(new MessageResponseDto("Card updated successfully"));
    }
}
