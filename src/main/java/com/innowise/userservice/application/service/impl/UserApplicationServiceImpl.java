package com.innowise.userservice.application.service.impl;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.mapper.PageMapper;
import com.innowise.userservice.application.mapper.PaymentCardMapper;
import com.innowise.userservice.application.mapper.UserMapper;
import com.innowise.userservice.application.service.UserApplicationService;
import com.innowise.userservice.domain.model.PaymentCard;
import com.innowise.userservice.domain.model.User;
import com.innowise.userservice.domain.model.exception.FailedToPerformOperationException;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import com.innowise.userservice.domain.port.out.UserRepository;
import com.innowise.userservice.infrastructure.cache.CacheConfig;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheEvict;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCachePut;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheable;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCaching;
import com.innowise.userservice.infrastructure.persistence.specification.UserSpecification;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PageMapper pageMapper;
    private final PaymentCardMapper paymentCardMapper;

    @Override
    @Transactional
    @CustomCaching(
            evict = {
                    @CustomCacheEvict(cacheName = CacheConfig.USERS_FILTERED_AND_PAGED_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.FULL_USERS_CACHE, allEntries = true)
            },
            put = @CustomCachePut(cacheName = CacheConfig.USERS_CACHE, keySpEL = "#result.id")
    )
    public UserResponseDto createUser(CreateUserDto createUserDto) {
        User user = userRepository.findById(createUserDto.userId()).orElseGet(() -> userRepository.save(userMapper.toEntity(createUserDto)));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, UserNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, UserNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    @CustomCaching(
            evict = {
                    @CustomCacheEvict(cacheName = CacheConfig.USERS_FILTERED_AND_PAGED_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.FULL_USERS_CACHE, allEntries = true)
            },
            put = @CustomCachePut(cacheName = CacheConfig.USERS_CACHE, keySpEL = "#result.id")
    )
    public UserResponseDto updateUser(UpdateUserDto updateUserDto, Long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userMapper.updateEntity(updateUserDto, user);
        user = userRepository.save(user);
        return userMapper.toDto(user);
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
    @CustomCaching(
            evict = {
                    @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_FILTERED_AND_PAGED_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_VIA_USER_ID_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.FULL_USERS_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.USERS_CACHE, keyArgumentIndexes = {1})
            },
            put = @CustomCachePut(cacheName = CacheConfig.PAYMENT_CARDS_CACHE, keySpEL = "#result.id")
    )
    public PaymentCardResponseDto addCardByUserId(CreatePaymentCardDto createPaymentCardDto, Long userId) throws UserNotFoundException, MaxPaymentCardsExceededException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        PaymentCard card = paymentCardMapper.toEntity(createPaymentCardDto);
        user.addCard(card);
        user = userRepository.saveAndFlush(user);
        PaymentCard savedCard = user.getCards()
                .stream()
                .max(Comparator.comparing(PaymentCard::getId))
                .orElseThrow();
        return paymentCardMapper.toDto(savedCard);
    }

    @Override
    @Transactional
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, UserNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, UserNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    @CustomCaching(
            evict = {
                    @CustomCacheEvict(cacheName = CacheConfig.USERS_FILTERED_AND_PAGED_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.FULL_USERS_CACHE, allEntries = true)
            },
            put = @CustomCachePut(cacheName = CacheConfig.USERS_CACHE, keySpEL = "#result.id")
    )
    public UserResponseDto deactivateUserById(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.setActiveById(user.getId(), false);
        user.setActive(false);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, UserNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, UserNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    @CustomCaching(
            evict = {
                    @CustomCacheEvict(cacheName = CacheConfig.USERS_FILTERED_AND_PAGED_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.FULL_USERS_CACHE, allEntries = true)
            },
            put = @CustomCachePut(cacheName = CacheConfig.USERS_CACHE, keySpEL = "#result.id")
    )
    public UserResponseDto activateUserById(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.setActiveById(user.getId(), true);
        user.setActive(true);
        return userMapper.toDto(user);
    }

    @Override
    @CustomCacheable(cacheName = CacheConfig.USERS_FILTERED_AND_PAGED_CACHE, keyArgumentIndexes = {0,1})
    public PageResponseDto<UserResponseDto> getUsers(UserFilter filter, Pageable pageable) {
        return pageMapper.toDto(userRepository.findAll(UserSpecification.fromUserFilter(filter), pageable).map(userMapper::toDto));
    }

    @Override
    @CustomCacheable(cacheName = CacheConfig.FULL_USERS_CACHE, keyArgumentIndexes = {0})
    public FullUserResponseDto getUserById(Long userId) throws UserNotFoundException {
       User user = userRepository.findByIdWithCards(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toFullDto(user, paymentCardMapper.toDtoList(user.getCards()));
    }

    @Recover
    public void recoverUpdate(Throwable e, UpdateUserDto updateUserDto, Long userId) throws FailedToPerformOperationException {
        log.error("Failed to update user with id {} after retries", userId, e);
        throw new FailedToPerformOperationException("Unable to update user with id: " + userId);
    }

    @Recover
    public void recoverChangeActiveStatus(Throwable e, Long id) throws FailedToPerformOperationException {
        log.error("Failed t o change user status for user with id {} after retries", id, e);
        throw new FailedToPerformOperationException("Unable to change user status for user with id: " + id);
    }

    @Recover
    public void recoverAddCard(Throwable e, CreatePaymentCardDto createPaymentCardDto, Long userId) throws FailedToPerformOperationException {
        log.error("Failed to add card for user with id {} after retries", userId, e);
        throw new FailedToPerformOperationException("Unable to add card for user with id: " + userId);
    }
}
