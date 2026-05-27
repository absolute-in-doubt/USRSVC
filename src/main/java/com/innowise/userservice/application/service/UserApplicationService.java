package com.innowise.userservice.application.service;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import org.springframework.data.domain.Pageable;

public interface UserApplicationService {

    UserResponseDto createUser(CreateUserDto createUserDto);

    UserResponseDto updateUser(UpdateUserDto updateUserDto, Long userId) throws UserNotFoundException;

    UserResponseDto addCardByUserId(CreatePaymentCardDto createPaymentCardDto, Long userId) throws UserNotFoundException, MaxPaymentCardsExceededException;

    UserResponseDto deactivateUserById(Long id) throws UserNotFoundException;

    UserResponseDto activateUserById(Long id) throws UserNotFoundException;

    PageResponseDto<UserResponseDto> getUsers(UserFilter filter, Pageable pageable);

    FullUserResponseDto getUserById(Long id) throws UserNotFoundException;
}
