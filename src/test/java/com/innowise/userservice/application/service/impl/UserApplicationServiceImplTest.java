package com.innowise.userservice.application.service.impl;

import com.innowise.userservice.TestcontainersConfiguration;
import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.mapper.*;
import com.innowise.userservice.application.service.UserApplicationService;
import com.innowise.userservice.domain.model.*;
import com.innowise.userservice.domain.model.exception.*;
import com.innowise.userservice.domain.port.out.PaymentCardRepository;
import com.innowise.userservice.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@Testcontainers
@SpringBootTest
class UserApplicationServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository pcRepo;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    @Autowired
    private UserApplicationService service;

    private User user;
    private  UserResponseDto userResponseDto;
    private CreateUserDto createUserDto;
    private UpdateUserDto updateUserDto;
    private CreatePaymentCardDto createPaymentCardDto;
    private PaymentCard paymentCard;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user = new User();
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("email");
        user.setActive(true);

        userResponseDto = new UserResponseDto(1L, "firstName", "lastName", "email", true);
        createUserDto = new CreateUserDto("firstName", "lastName", LocalDate.of(2005, 1, 1), "email");
        updateUserDto = new UpdateUserDto("newFirstName", "newLastName", "email", true);
        createPaymentCardDto = new CreatePaymentCardDto("1234567887654321", "John Doe", LocalDate.of(2028, 1, 1));

        paymentCard = new PaymentCard();
        paymentCard.setCardNumber("1234567887654321");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2028, 1, 1));

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createUser() {
        UserResponseDto result = service.createUser(createUserDto);
        assertEquals(userResponseDto.firstName(), result.firstName());
        assertEquals(userResponseDto.lastName(), result.lastName());
        assertEquals(userResponseDto.email(), result.email());
        assertTrue(userRepository.findById(result.id()).isPresent());
        assertTrue(userResponseDto.active());
    }

    @Test
    void updateUser() throws UserNotFoundException {
        user = userRepository.save(user);
        service.updateUser(updateUserDto, user.getId());
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(updateUserDto.firstName(), updated.getFirstName());
        assertEquals(updateUserDto.lastName(), updated.getLastName());
        assertEquals(updateUserDto.email(), updated.getEmail());
        assertEquals(updateUserDto.active(),updated.isActive());
    }

    @Test
    void addCardByUserId() throws UserNotFoundException, MaxPaymentCardsExceededException {
        user = userRepository.save(user);
        service.addCardByUserId(createPaymentCardDto, user.getId());
        user = userRepository.findById(user.getId()).orElseThrow();
        List<PaymentCard> pcs = pcRepo.findByUserId(user.getId());
        PaymentCard paymentCardResult = pcs.getFirst();
        assertEquals(1, pcs.size());
        assertEquals(paymentCard.getCardNumber(), paymentCardResult.getCardNumber());
        assertTrue(paymentCardResult.isActive());
    }

    @Test
    void deactivateUserById() throws UserNotFoundException {
        user = userRepository.save(user);
        UserResponseDto result = service.deactivateUserById(user.getId());
        assertFalse(userRepository.findById(user.getId()).orElseThrow().isActive());
    }

    @Test
    void activateUserById() throws UserNotFoundException {
        user.setActive(false);
        user = userRepository.save(user);
        UserResponseDto result = service.activateUserById(user.getId());
        assertTrue(userRepository.findById(user.getId()).orElseThrow().isActive());
    }

    @Test
    void getUsers() {
        user = userRepository.save(user);
        UserFilter filter = new UserFilter(user.getFirstName(), user.getLastName());
        Page<UserResponseDto> result = service.getUsers(filter, pageable);
        assertEquals(1, result.getTotalElements());
        UserResponseDto firstElement = result.getContent().get(0);
        assertEquals(firstElement.active(), user.isActive());
        assertEquals(firstElement.firstName(), user.getFirstName());
        assertEquals(firstElement.lastName(), user.getLastName());
        assertEquals(firstElement.email(), user.getEmail());
    }

    @Test
    void getUserById() throws UserNotFoundException {
        user = userRepository.save(user);
        UserResponseDto result = service.getUserById(user.getId());
        assertEquals(result.active(), user.isActive());
        assertEquals(result.firstName(), user.getFirstName());
        assertEquals(result.lastName(), user.getLastName());
        assertEquals(result.email(), user.getEmail());
    }
}
