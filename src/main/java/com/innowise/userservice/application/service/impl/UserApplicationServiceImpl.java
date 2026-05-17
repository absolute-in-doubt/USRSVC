package com.innowise.userservice.application.service.impl;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.mapper.PaymentCardMapper;
import com.innowise.userservice.application.mapper.UserMapper;
import com.innowise.userservice.application.service.UserApplicationService;
import com.innowise.userservice.domain.model.PaymentCard;
import com.innowise.userservice.domain.model.User;
import com.innowise.userservice.domain.model.exception.FailedToPerformOperationException;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import com.innowise.userservice.domain.port.out.UserRepository;
import com.innowise.userservice.infrastructure.persistence.specification.UserSpecification;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    public void createUser(CreateUserDto createUserDto) {
        userRepository.save(userMapper.toEntity(createUserDto));
    }

    @Override
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, UserNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, UserNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    public void updateUser(UpdateUserDto updateUserDto, Long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userMapper.updateEntity(updateUserDto, user);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, UserNotFoundException.class, MaxPaymentCardsExceededException.class},
            notRecoverable = {DataIntegrityViolationException.class, UserNotFoundException.class, MaxPaymentCardsExceededException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    public void addCardByUserId(CreatePaymentCardDto createPaymentCardDto, Long userId) throws UserNotFoundException, MaxPaymentCardsExceededException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        PaymentCard card = paymentCardMapper.toEntity(createPaymentCardDto);
        user.addCard(card);
        userRepository.save(user);
    }

    @Override
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, UserNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, UserNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    public void deactivateUserById(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, UserNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, UserNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    public void activateUserById(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public Page<UserResponseDto> getUsers(UserFilter filter, Pageable pageable) {
        return userRepository.findAll(UserSpecification.fromUserFilter(filter), pageable).map(userMapper::toDto);
    }

    @Override
    public UserResponseDto getUserById(Long id) throws UserNotFoundException {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Recover
    public void recoverUpdate(Throwable e, UpdateUserDto updateUserDto, Long userId) throws FailedToPerformOperationException {
        log.error("Failed to update user with id {} after retries", userId, e);
        throw new FailedToPerformOperationException("Unable to update user with id: " + userId);
    }

    @Recover
    public void recoverChangeActiveStatus(Throwable e, Long id) throws FailedToPerformOperationException {
        log.error("Failed to change user status for user with id {} after retries", id, e);
        throw new FailedToPerformOperationException("Unable to change user status for user with id: " + id);
    }

    @Recover
    public void recoverAddCard(Throwable e, CreatePaymentCardDto createPaymentCardDto, Long userId) throws FailedToPerformOperationException {
        log.error("Failed to add card for user with id {} after retries", userId, e);
        throw new FailedToPerformOperationException("Unable to add card for user with id: " + userId);
    }
}
