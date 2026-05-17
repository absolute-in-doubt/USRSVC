package com.innowise.userservice.application.service;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserApplicationService {

    void createUser(CreateUserDto createUserDto);

    void updateUser(UpdateUserDto updateUserDto, Long userId) throws UserNotFoundException;

    void addCardByUserId(CreatePaymentCardDto createPaymentCardDto, Long userId) throws UserNotFoundException, MaxPaymentCardsExceededException;

    void deactivateUserById(Long id) throws UserNotFoundException;

    void activateUserById(Long id) throws UserNotFoundException;

    Page<UserResponseDto> getUsers(UserFilter filter, Pageable pageable);

    UserResponseDto getUserById(Long id) throws UserNotFoundException;
}
