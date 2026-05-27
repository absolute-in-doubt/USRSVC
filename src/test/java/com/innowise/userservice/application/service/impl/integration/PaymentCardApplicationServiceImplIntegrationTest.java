package com.innowise.userservice.application.service.impl.integration;

import com.innowise.userservice.TestcontainersConfiguration;
import com.innowise.userservice.application.dto.PageResponseDto;
import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.application.mapper.PaymentCardMapper;
import com.innowise.userservice.application.service.PaymentCardApplicationService;
import com.innowise.userservice.domain.model.PaymentCard;
import com.innowise.userservice.domain.model.User;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.domain.port.out.PaymentCardRepository;
import com.innowise.userservice.domain.port.out.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Import(TestcontainersConfiguration.class)
@Testcontainers
@SpringBootTest
class PaymentCardApplicationServiceImplIntegrationTest {

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @Autowired
    private PaymentCardApplicationService service;

    private User user;
    private User user2;
    private PaymentCard paymentCard;
    private PaymentCardResponseDto paymentCardResponseDto;
    private UpdatePaymentCardDto updatePaymentCardDto;
    private PaymentCardFilter filter;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        user2 = new User();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setActive(true);
        user2 = userRepository.save(user2);

        paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setCardNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2028, 12, 31));
        paymentCard.setActive(true);
        paymentCard.setCreatedAt(LocalDateTime.now());
        paymentCard.setUpdatedAt(LocalDateTime.now());
        paymentCard = paymentCardRepository.save(paymentCard);

        // Create DTOs
        paymentCardResponseDto = new PaymentCardResponseDto(
                paymentCard.getId(),
                "1234567890123456",
                "John Doe",
                "2028-12-31",
                true
        );

        updatePaymentCardDto = new UpdatePaymentCardDto(
                "9876543210987654",
                "Jane Smith",
                LocalDate.of(2029,11,30),
                false
        );

        filter = new PaymentCardFilter("John", "Doe");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getPaymentCardById_Success() throws PaymentCardNotFoundException {
        PaymentCardResponseDto result = service.getPaymentCardById(paymentCard.getId());
        
        assertNotNull(result);
        assertEquals(paymentCardResponseDto.id(), result.id());
        assertEquals(paymentCardResponseDto.cardNumber(), result.cardNumber());
        assertEquals(paymentCardResponseDto.holder(), result.holder());
        assertEquals(paymentCardResponseDto.expirationDate(), result.expirationDate());
        assertEquals(paymentCardResponseDto.active(), result.active());
    }

    @Test
    void getPaymentCardById_NotFound() {
        assertThrows(PaymentCardNotFoundException.class, () -> 
            service.getPaymentCardById(999L));
    }

    @Test
    void getAllPaymentCards_Success() {
        PageResponseDto<PaymentCardResponseDto> result = service.getAllPaymentCards(filter, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        PaymentCardResponseDto firstElement = result.content().get(0);
        assertEquals(paymentCardResponseDto.id(), firstElement.id());
        assertEquals(paymentCardResponseDto.cardNumber(), firstElement.cardNumber());
        assertEquals(paymentCardResponseDto.holder(), firstElement.holder());
    }

    @Test
    void getAllPaymentCards_EmptyFilter() {
        PaymentCardFilter filter = new PaymentCardFilter(null, null);
        PageResponseDto<PaymentCardResponseDto> result = service.getAllPaymentCards(filter, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.totalElements());
    }

    @Test
    void deactivateCardById_Success() throws PaymentCardNotFoundException {
        service.deactivateCardById(paymentCard.getId());
        
        PaymentCard updatedCard = paymentCardRepository.findById(paymentCard.getId()).orElseThrow();
        assertFalse(updatedCard.isActive());
    }

    @Test
    void deactivateCardById_NotFound() {
        assertThrows(PaymentCardNotFoundException.class, () -> 
            service.deactivateCardById(999L));
    }

    @Test
    void activateCardById_Success() throws PaymentCardNotFoundException {
        // First deactivate the card
        paymentCard.setActive(false);
        paymentCardRepository.save(paymentCard);
        
        service.activateCardById(paymentCard.getId());
        
        PaymentCard updatedCard = paymentCardRepository.findById(paymentCard.getId()).orElseThrow();
        assertTrue(updatedCard.isActive());
    }

    @Test
    void activateCardById_NotFound() {
        assertThrows(PaymentCardNotFoundException.class, () -> 
            service.activateCardById(999L));
    }

    @Test
    void getCardsByUserId_Success() {
        List<PaymentCardResponseDto> result = service.getCardsByUserId(user.getId());
        
        assertNotNull(result);
        assertEquals(1, result.size());
        PaymentCardResponseDto firstCard = result.get(0);
        assertEquals(paymentCardResponseDto.id(), firstCard.id());
        assertEquals(paymentCardResponseDto.cardNumber(), firstCard.cardNumber());
        assertEquals(paymentCardResponseDto.holder(), firstCard.holder());
    }

    @Test
    void getCardsByUserId_EmptyList() {
        List<PaymentCardResponseDto> result = service.getCardsByUserId(user2.getId());
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateCard_Success() throws PaymentCardNotFoundException {
        service.updateCard(updatePaymentCardDto, paymentCard.getId());
        
        PaymentCard updatedCard = paymentCardRepository.findById(paymentCard.getId()).orElseThrow();
        assertEquals(updatePaymentCardDto.cardNumber(), updatedCard.getCardNumber());
        assertEquals(updatePaymentCardDto.holder(), updatedCard.getHolder());
        assertEquals(updatePaymentCardDto.expirationDate(), updatedCard.getExpirationDate());
        assertFalse(updatedCard.isActive());
    }

    @Test
    void updateCard_NotFound() {
        assertThrows(PaymentCardNotFoundException.class, () -> 
            service.updateCard(updatePaymentCardDto, 999L));
    }
}