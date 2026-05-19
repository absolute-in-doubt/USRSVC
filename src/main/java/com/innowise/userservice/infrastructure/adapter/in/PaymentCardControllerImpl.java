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
@RequestMapping("api/v1/cards")
public class PaymentCardControllerImpl implements PaymentCardController {

    private final PaymentCardApplicationService service;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable("id") Long id) throws PaymentCardNotFoundException {
        return ResponseEntity.ok(service.getPaymentCardById(id));
    }

    public ResponseEntity<Page<PaymentCardResponseDto>> getAllCards(@ParameterObject PaymentCardFilter filter,
                                                                    @ParameterObject @PageableDefault Pageable pageable){
        return ResponseEntity.ok(service.getAllPaymentCards(filter, pageable));
    }

    @GetMapping
    public ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(service.getCardsByUserId(userId));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MessageResponseDto> deactivateCardById(@PathVariable("id") Long id) throws PaymentCardNotFoundException {
        service.deactivateCardById(id);
        return ResponseEntity.ok(new MessageResponseDto("Card deactivated successfully"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<MessageResponseDto> activateCardById(@PathVariable("id") Long id) throws PaymentCardNotFoundException {
        service.activateCardById(id);
        return ResponseEntity.ok(new MessageResponseDto("Card activated successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponseDto> updateCard(@RequestBody UpdatePaymentCardDto updatePaymentCardDto,
                                                        @PathVariable("id") Long id) throws PaymentCardNotFoundException {
        service.updateCard(updatePaymentCardDto, id);
        return ResponseEntity.ok(new MessageResponseDto("Card updated successfully"));
    }
}
