package com.innowise.userservice.domain.port.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface UserController {

    ResponseEntity<MessageResponseDto> createUser(CreateUserDto createUserDto);

    ResponseEntity<MessageResponseDto> updateUser(UpdateUserDto updateUserDto, Long userId) throws UserNotFoundException;

    ResponseEntity<MessageResponseDto> addCardByUserId(CreatePaymentCardDto createPaymentCardDto, Long userId)
            throws UserNotFoundException, MaxPaymentCardsExceededException;

    ResponseEntity<MessageResponseDto> deactivateUserById(Long id) throws UserNotFoundException;

    ResponseEntity<MessageResponseDto> activateUserById(Long id) throws UserNotFoundException;

    ResponseEntity<Page<UserResponseDto>> getUsers(UserFilter filter, Pageable pageable);

    ResponseEntity<UserResponseDto> getUserById(Long id) throws UserNotFoundException;
}
