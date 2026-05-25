package com.innowise.userservice.application.service.impl.unit;

import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.application.mapper.PaymentCardMapper;
import com.innowise.userservice.application.mapper.PaymentCardMapperImpl;
import com.innowise.userservice.application.service.impl.PaymentCardApplicationServiceImpl;
import com.innowise.userservice.domain.model.PaymentCard;
import com.innowise.userservice.domain.model.User;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.domain.port.out.PaymentCardRepository;
import com.innowise.userservice.infrastructure.persistence.specification.PaymentCardSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Slf4j
@ExtendWith(MockitoExtension.class)
public class PaymentCardApplicationServiceImplUnitTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private EntityManager em;

    private PaymentCardApplicationServiceImpl service;


    @BeforeEach
    void mocksSetUp() {
        service = new PaymentCardApplicationServiceImpl(
                paymentCardRepository,
                new PaymentCardMapperImpl(),
                em
        );
    }
//    {
//        Field mapperField = Arrays.stream(service.getClass().getFields())
//                .filter(f -> f.getType().equals(PaymentCardMapper.class)).findFirst().get();
//        mapperField.setAccessible(true);
//        try {
//            mapperField.set(service, new PaymentCardMapperImpl()); //May fail if target is empty
//        } catch (IllegalAccessException | IllegalArgumentException e) {
//            log.error("Failed to inject mapper into the paymentCardService" +
//                    "\ntry mvn clean compile in case PaymentCardMapperImpl is not present");
//            throw new RuntimeException(e);
//        }
//    }

    private User user;
    private User user2;
    private PaymentCard paymentCard;
    private PaymentCardResponseDto paymentCardResponseDto;
    private UpdatePaymentCardDto updatePaymentCardDto;
    private PaymentCardFilter filter;
    private Pageable pageable;

    private List<User> users = new ArrayList<>();
    private List<PaymentCard> paymentCards = new ArrayList<>();

    @BeforeEach
    void setUp() {

        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setActive(true);
        users.add(user);

        user2 = new User();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setActive(true);
        users.add(user2);


        paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setCardNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2028, 12, 31));
        paymentCard.setActive(true);
        paymentCard.setCreatedAt(LocalDateTime.now());
        paymentCard.setUpdatedAt(LocalDateTime.now());
        paymentCards.add(paymentCard);


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
        Mockito.when(paymentCardRepository.findById(Mockito.anyLong()))
                .thenAnswer(
                        invocationOnMock ->
                            Optional.of(paymentCard)
                );

        PaymentCardResponseDto result = service.getPaymentCardById(Mockito.anyLong());

        assertNotNull(result);
        assertEquals(paymentCardResponseDto.id(), result.id());
        assertEquals(paymentCardResponseDto.cardNumber(), result.cardNumber());
        assertEquals(paymentCardResponseDto.holder(), result.holder());
        assertEquals(paymentCardResponseDto.expirationDate(), result.expirationDate());
        assertEquals(paymentCardResponseDto.active(), result.active());

        Mockito.verify(paymentCardRepository).findById(Mockito.anyLong());
    }

    @Test
    void getPaymentCardById_NotFound() {
        Mockito.when(paymentCardRepository.findById(Mockito.anyLong()))
                .thenReturn(java.util.Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () ->
                service.getPaymentCardById(999L));

        Mockito.verify(paymentCardRepository).findById(999L);
    }

    @Test
    void getAllPaymentCards_Success() {
        Mockito.when(paymentCardRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(paymentCards));

        Page<PaymentCardResponseDto> result = service.getAllPaymentCards(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        PaymentCardResponseDto firstElement = result.getContent().get(0);
        assertEquals(paymentCardResponseDto.id(), firstElement.id());
        assertEquals(paymentCardResponseDto.cardNumber(), firstElement.cardNumber());
        assertEquals(paymentCardResponseDto.holder(), firstElement.holder());

        Mockito.verify(paymentCardRepository).findAll(Mockito.any(Specification.class), Mockito.eq(pageable));
    }


    @Test
    void getAllPaymentCards_EmptyFilter() {
        Mockito.when(paymentCardRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl(paymentCards));

        PaymentCardFilter filter = new PaymentCardFilter(null, null);
        Page<PaymentCardResponseDto> result = service.getAllPaymentCards(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());


        Mockito.verify(paymentCardRepository).findAll(Mockito.any(Specification.class), Mockito.eq(pageable));
    }

    @Test
    void deactivateCardById_Success() throws PaymentCardNotFoundException {
        Mockito.when(paymentCardRepository.findById(Mockito.anyLong()))
                .thenReturn(java.util.Optional.ofNullable(paymentCard));
        Mockito.doNothing().when(paymentCardRepository).setActiveById(Mockito.anyLong(),Mockito.anyBoolean());
        Mockito.doNothing().when(em).lock(Mockito.any(User.class), Mockito.eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));

        service.deactivateCardById(Mockito.anyLong());

        Mockito.verify(paymentCardRepository).findById(Mockito.anyLong());
        Mockito.verify(paymentCardRepository).setActiveById(Mockito.anyLong(), Mockito.anyBoolean());
        Mockito.verify(em).lock(Mockito.any(User.class), Mockito.eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));
    }

    @Test
    void deactivateCardById_NotFound() {
        Mockito.when(paymentCardRepository.findById(Mockito.anyLong()))
                .thenReturn(java.util.Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () ->
                service.deactivateCardById(999L));
    }

    @Test
    void activateCardById_Success() throws PaymentCardNotFoundException {

        Mockito.when(paymentCardRepository.findById(Mockito.anyLong()))
                .thenReturn(java.util.Optional.ofNullable(paymentCard));
        Mockito.doNothing().when(paymentCardRepository).setActiveById(Mockito.anyLong(),Mockito.anyBoolean());
        Mockito.doNothing().when(em).lock(Mockito.any(User.class), Mockito.eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));


        service.activateCardById(Mockito.anyLong());

        Mockito.verify(paymentCardRepository).findById(Mockito.anyLong());
        Mockito.verify(paymentCardRepository).setActiveById(Mockito.anyLong(), Mockito.anyBoolean());
        Mockito.verify(em).lock(Mockito.any(User.class), Mockito.eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));
    }

    @Test
    void activateCardById_NotFound() {
        Mockito.when(paymentCardRepository.findById(Mockito.anyLong()))
                .thenReturn(java.util.Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () ->
                service.activateCardById(999L));
    }

    @Test
    void getCardsByUserId_Success() {
        Mockito.when(paymentCardRepository.findByUserId(Mockito.anyLong()))
                .thenReturn(paymentCards);

        List<PaymentCardResponseDto> result = service.getCardsByUserId(Mockito.anyLong());

        assertNotNull(result);
        assertEquals(1, result.size());
        PaymentCardResponseDto firstCard = result.get(0);
        assertEquals(paymentCardResponseDto.id(), firstCard.id());
        assertEquals(paymentCardResponseDto.cardNumber(), firstCard.cardNumber());
        assertEquals(paymentCardResponseDto.holder(), firstCard.holder());
    }

    @Test
    void getCardsByUserId_EmptyList() {
        Mockito.when(paymentCardRepository.findByUserId(Mockito.anyLong()))
                .thenReturn(Collections.emptyList());
        List<PaymentCardResponseDto> result = service.getCardsByUserId(Mockito.anyLong());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateCard_Success() throws PaymentCardNotFoundException {
        Mockito.when(paymentCardRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(paymentCard));
        Mockito.doNothing().when(em).lock(Mockito.any(User.class), Mockito.eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));


        service.updateCard(updatePaymentCardDto, Mockito.anyLong());

        Mockito.verify(paymentCardRepository).findById(Mockito.anyLong());
        Mockito.verify(em).lock(Mockito.any(User.class), Mockito.eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));
    }

    @Test
    void updateCard_NotFound() {
        Mockito.when(paymentCardRepository.findById(Mockito.anyLong()))
                .thenReturn(java.util.Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () ->
                service.updateCard(updatePaymentCardDto, 999L));
    }
}
