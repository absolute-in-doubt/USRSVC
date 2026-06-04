package com.innowise.userservice.application.service.impl.unit;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.mapper.PageMapper;
import com.innowise.userservice.application.mapper.PaymentCardMapper;
import com.innowise.userservice.application.mapper.UserMapper;
import com.innowise.userservice.application.service.impl.UserApplicationServiceImpl;
import com.innowise.userservice.domain.model.PaymentCard;
import com.innowise.userservice.domain.model.User;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import com.innowise.userservice.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class UserApplicationServiceImplUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private PageMapper pageMapper;

    private UserApplicationServiceImpl service;

    private User user;
    private User user2;
    private UserResponseDto userResponseDto;
    private FullUserResponseDto fullUserResponseDto;
    private CreateUserDto createUserDto;
    private UpdateUserDto updateUserDto;
    private CreatePaymentCardDto createPaymentCardDto;
    private UserFilter userFilter;
    private Pageable pageable;
    private PageResponseDto<UserResponseDto> pageResponse;
    private PageResponseDto<UserResponseDto> emptyPageResponse;
    private List<User> users;
    private List<PaymentCard> paymentCards;

    @BeforeEach
    void setUp() {
        service = new UserApplicationServiceImpl(userRepository, userMapper, pageMapper, paymentCardMapper);

        // Setup test data
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        user2 = new User();
        user2.setId(1L);
        user2.setFirstName("John");
        user2.setLastName("Doe");
        user2.setEmail("john.doe@example.com");
        user2.setActive(true);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());

        users = new ArrayList<>();
        users.add(user);
        users.add(user2);

        paymentCards = new ArrayList<>();
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setId(1L);
        paymentCard.setCardNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2028, 12, 31));
        paymentCard.setActive(true);
        paymentCard.setUser(user);
        paymentCards.add(paymentCard);
        user.setCards(paymentCards);

        // Setup DTOs
        userResponseDto = new UserResponseDto(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                true
        );

        UserResponseDto userResponseDto2 = new UserResponseDto(
                2L,
                "John",
                "Doe",
                "john.doe@example.com",
                true
        );

        fullUserResponseDto = new FullUserResponseDto(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                true,
                Collections.emptyList()
        );

        createUserDto = new CreateUserDto(
                1L,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe@example.com"
        );

        updateUserDto = new UpdateUserDto(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                true
        );

        createPaymentCardDto = new CreatePaymentCardDto(
                "9876543210987654",
                "Jane Smith",
                LocalDate.of(2029, 11, 30)
        );

        userFilter = new UserFilter("John", "Doe");
        pageable = PageRequest.of(0, 10);

        pageResponse = new PageResponseDto<>(
                List.of(userResponseDto, userResponseDto2),
                false,
                true,
                true,
                0,
                2,
                1,
                2,
                1
                );

        emptyPageResponse = new PageResponseDto<>(
                List.of(),
                true,
                true,
                true,
                0,
                0,
                0,
                0,
                0
        );
    }

    @Test
    void createUser_Success() {
        Mockito.when(userMapper.toEntity(createUserDto)).thenReturn(user);
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = service.createUser(createUserDto);

        assertNotNull(result);
        assertEquals(userResponseDto.id(), result.id());
        assertEquals(userResponseDto.firstName(), result.firstName());
        assertEquals(userResponseDto.lastName(), result.lastName());
        assertEquals(userResponseDto.email(), result.email());

        Mockito.verify(userMapper).toEntity(createUserDto);
        Mockito.verify(userRepository).save(user);
        Mockito.verify(userMapper).toDto(user);
    }

    @Test
    void updateUser_Success() throws UserNotFoundException {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        updatedUser.setEmail("jane.smith@example.com");
        updatedUser.setActive(true);

        UserResponseDto updatedResponseDto = new UserResponseDto(
                userId,
                "Jane",
                "Smith",
                "jane.smith@example.com",
                true
        );

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.doNothing().when(userMapper).updateEntity(updateUserDto, user);
        Mockito.when(userRepository.save(user)).thenReturn(updatedUser);
        Mockito.when(userMapper.toDto(updatedUser)).thenReturn(updatedResponseDto);

        UserResponseDto result = service.updateUser(updateUserDto, userId);

        assertNotNull(result);
        assertEquals(updatedResponseDto.id(), result.id());
        assertEquals(updatedResponseDto.firstName(), result.firstName());
        assertEquals(updatedResponseDto.lastName(), result.lastName());
        assertEquals(updatedResponseDto.email(), result.email());

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper).updateEntity(updateUserDto, user);
        Mockito.verify(userRepository).save(user);
        Mockito.verify(userMapper).toDto(updatedUser);
    }

    @Test
    void updateUser_NotFound() {
        Long userId = 999L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> 
            service.updateUser(updateUserDto, userId));

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper, Mockito.never()).updateEntity(any(), any());
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    @Test
    void addCardByUserId_Success() throws UserNotFoundException, MaxPaymentCardsExceededException {
        Long userId = 1L;
        PaymentCard newCard = new PaymentCard();
        newCard.setId(3L);
        newCard.setCardNumber("9876543210987654");
        newCard.setHolder("Jane Smith");
        newCard.setExpirationDate(LocalDate.of(2029, 11, 30));
        newCard.setActive(true);
        newCard.setUser(user);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");
        updatedUser.setEmail("john.doe@example.com");
        updatedUser.setActive(true);
        updatedUser.setCards(List.of(paymentCards.get(0), newCard));

        PaymentCardResponseDto updatedResponseDto = new PaymentCardResponseDto(
                2L,
                user.getId(),
                "9876543210987654",
                "Jane Smith",
                LocalDate.of(2029, 11, 30).toString(),
                true
        );

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(paymentCardMapper.toEntity(Mockito.eq(createPaymentCardDto)))
                .thenReturn(newCard);
        Mockito.when(userRepository.saveAndFlush(Mockito.any(User.class)))
                .thenReturn(updatedUser);
        Mockito.when(paymentCardMapper.toDto(Mockito.any(PaymentCard.class)))
                .thenReturn(updatedResponseDto);

        PaymentCardResponseDto result = service.addCardByUserId(createPaymentCardDto, userId, user.getId(), true);

        assertNotNull(result);
        assertEquals(updatedResponseDto.id(), result.id());
        assertEquals(updatedResponseDto.userId(), result.userId());
        assertEquals(updatedResponseDto.cardNumber(), result.cardNumber());
        assertEquals(updatedResponseDto.holder(), result.holder());
        assertEquals(updatedResponseDto.active(), result.active());

        Mockito.verify(userRepository).findById(Mockito.eq(userId));
        Mockito.verify(paymentCardMapper).toEntity(Mockito.eq(createPaymentCardDto));
        Mockito.verify(userRepository).saveAndFlush(Mockito.any(User.class));
        Mockito.verify(paymentCardMapper).toDto(Mockito.any(PaymentCard.class));
    }

    @Test
    void addCardByUserId_UserNotFound() {
        Long userId = 999L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> 
            service.addCardByUserId(createPaymentCardDto, userId, userId, true));

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(paymentCardMapper, Mockito.never()).toEntity(any());
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    @Test
    void addCardByUserId_MaxCardsExceeded() throws MaxPaymentCardsExceededException {
        Long userId = user2.getId();
        
        // Add maximum cards to user
        for (int i = 0; i < 5; i++) {
            PaymentCard card = new PaymentCard();
            card.setId((long) (i + 10));
            card.setCardNumber("111122223333444" + i);
            card.setHolder("Test Holder " + i);
            card.setExpirationDate(LocalDate.of(2028, 12, 31));
            card.setActive(true);
            user2.addCard(card);
        }
        
        PaymentCard newCard = new PaymentCard();
        newCard.setId(userId);
        newCard.setCardNumber("9876543210987654");
        newCard.setHolder("Jane Smith");
        newCard.setExpirationDate(LocalDate.of(2029, 11, 30));
        newCard.setActive(true);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user2));
        Mockito.when(paymentCardMapper.toEntity(createPaymentCardDto)).thenReturn(newCard);

        assertThrows(MaxPaymentCardsExceededException.class, () -> 
            service.addCardByUserId(createPaymentCardDto, user2.getId(), user2.getId(), true));

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(paymentCardMapper).toEntity(createPaymentCardDto);
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    @Test
    void deactivateUserById_Success() throws UserNotFoundException {
        Long userId = 1L;
        User deactivatedUser = new User();
        deactivatedUser.setId(userId);
        deactivatedUser.setFirstName("John");
        deactivatedUser.setLastName("Doe");
        deactivatedUser.setEmail("john.doe@example.com");
        deactivatedUser.setActive(false);

        UserResponseDto deactivatedResponseDto = new UserResponseDto(
                userId,
                "John",
                "Doe",
                "john.doe@example.com",
                false
        );

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.doNothing().when(userRepository).setActiveById(userId, false);
        Mockito.when(userMapper.toDto(user)).thenReturn(deactivatedResponseDto);

        UserResponseDto result = service.deactivateUserById(userId);

        assertNotNull(result);
        assertEquals(deactivatedResponseDto.id(), result.id());
        assertFalse(result.active());
        assertFalse(user.isActive());

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userRepository).setActiveById(userId, false);
        Mockito.verify(userMapper).toDto(user);
    }

    @Test
    void deactivateUserById_NotFound() {
        Long userId = 999L;
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> 
            service.deactivateUserById(userId));

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userRepository, Mockito.never()).setActiveById(userId, false);
    }

    @Test
    void activateUserById_Success() throws UserNotFoundException {
        Long userId = 1L;
        user.setActive(false); // Start with inactive user
        
        User activatedUser = new User();
        activatedUser.setId(userId);
        activatedUser.setFirstName("John");
        activatedUser.setLastName("Doe");
        activatedUser.setEmail("john.doe@example.com");
        activatedUser.setActive(true);

        UserResponseDto activatedResponseDto = new UserResponseDto(
                userId,
                "John",
                "Doe",
                "john.doe@example.com",
                true
        );

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.doNothing().when(userRepository).setActiveById(userId, true);
        Mockito.when(userMapper.toDto(user)).thenReturn(activatedResponseDto);

        UserResponseDto result = service.activateUserById(userId);

        assertNotNull(result);
        assertEquals(activatedResponseDto.id(), result.id());
        assertTrue(result.active());
        assertTrue(user.isActive());

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userRepository).setActiveById(userId, true);
        Mockito.verify(userMapper).toDto(user);
    }

    @Test
    void activateUserById_NotFound() {
        Long userId = 999L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> 
            service.activateUserById(userId));

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userRepository, Mockito.never()).setActiveById(userId, true);
    }

    @Test
    void getUsers_Success() {
        Page<User> userPage = new PageImpl<>(users);
        Page<UserResponseDto> expectedPage = new PageImpl<>(List.of(userResponseDto));

        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(userPage);
        Mockito.when(userMapper.toDto(user)).thenReturn(userResponseDto);
        Mockito.when(pageMapper.toDto(Mockito.any(Page.class))).thenReturn(pageResponse);

        PageResponseDto<UserResponseDto> result = service.getUsers(userFilter, pageable);

        assertNotNull(result);
        assertEquals(users.size(), result.totalElements());
        assertEquals(userResponseDto.id(), result.content().get(0).id());
        assertEquals(userResponseDto.firstName(), result.content().get(0).firstName());
        assertEquals(userResponseDto.lastName(), result.content().get(0).lastName());

        Mockito.verify(userRepository).findAll(any(Specification.class), eq(pageable));
        Mockito.verify(userMapper).toDto(user);
    }

    @Test
    void getUsers_EmptyResult() {
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        Page<UserResponseDto> expectedEmptyPage = new PageImpl<>(Collections.emptyList());

        Mockito.when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);
        Mockito.when(pageMapper.toDto(Mockito.any(Page.class))).thenReturn(emptyPageResponse);

        PageResponseDto<UserResponseDto> result = service.getUsers(userFilter, pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());

        Mockito.verify(userRepository).findAll(any(Specification.class), eq(pageable));
        Mockito.verify(userMapper, Mockito.never()).toDto(any());
    }

    @Test
    void getUserById_Success() throws UserNotFoundException {
        Long userId = 1L;
        List<PaymentCardResponseDto> cardDtos = List.of(
            new PaymentCardResponseDto(1L, user.getId(), "1234567890123456", "John Doe", "2028-12-31", true)
        );

        Mockito.when(userRepository.findByIdWithCards(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(paymentCardMapper.toDtoList(paymentCards))
                .thenReturn(cardDtos);
        Mockito.when(userMapper.toFullDto(user, cardDtos))
                .thenReturn(fullUserResponseDto);

        FullUserResponseDto result = service.getUserById(userId, user.getId(), true);

        assertNotNull(result);
        assertEquals(fullUserResponseDto.id(), result.id());
        assertEquals(fullUserResponseDto.firstName(), result.firstName());
        assertEquals(fullUserResponseDto.lastName(), result.lastName());
        assertEquals(fullUserResponseDto.email(), result.email());

        Mockito.verify(userRepository).findByIdWithCards(userId);
        Mockito.verify(paymentCardMapper).toDtoList(paymentCards);
        Mockito.verify(userMapper).toFullDto(user, cardDtos);
    }

    @Test
    void getUserById_NotFound() {
        Long userId = 999L;
        Mockito.when(userRepository.findByIdWithCards(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> 
            service.getUserById(userId, userId, true));

        Mockito.verify(userRepository).findByIdWithCards(userId);
        Mockito.verify(paymentCardMapper, Mockito.never()).toDtoList(any());
        Mockito.verify(userMapper, Mockito.never()).toFullDto(any(), any());
    }
}