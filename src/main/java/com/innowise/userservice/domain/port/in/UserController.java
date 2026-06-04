package com.innowise.userservice.domain.port.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UserController {

    ResponseEntity<Void> createUser(@Valid CreateUserDto createUserDto);

    ResponseEntity<MessageResponseDto> updateUser(@Valid UpdateUserDto updateUserDto, Long userId) throws UserNotFoundException;

    ResponseEntity<MessageResponseDto> addCardByUserId(@Valid CreatePaymentCardDto createPaymentCardDto, Long userId,
            com.innowise.userservice.infrastructure.security.model.JwtUserDetails jwtUserDetails,
            org.springframework.security.core.Authentication authentication)
            throws UserNotFoundException, MaxPaymentCardsExceededException;

    ResponseEntity<Void> deactivateUserById( Long id) throws UserNotFoundException;

    ResponseEntity<Void> activateUserById(Long id) throws UserNotFoundException;

    ResponseEntity<PageResponseDto<UserResponseDto>> getUsers(UserFilter filter, Pageable pageable);

    ResponseEntity<FullUserResponseDto> getUserById(Long id,
            com.innowise.userservice.infrastructure.security.model.JwtUserDetails jwtUserDetails,
            org.springframework.security.core.Authentication authentication) throws UserNotFoundException;
}
