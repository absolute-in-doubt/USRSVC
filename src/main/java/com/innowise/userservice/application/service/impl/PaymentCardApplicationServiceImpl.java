package com.innowise.userservice.application.service.impl;

import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.application.mapper.PaymentCardMapper;
import com.innowise.userservice.application.service.PaymentCardApplicationService;
import com.innowise.userservice.domain.model.PaymentCard;
import com.innowise.userservice.domain.model.exception.FailedToPerformOperationException;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.domain.port.out.PaymentCardRepository;
import com.innowise.userservice.infrastructure.cache.CacheConfig;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheEvict;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheable;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCaching;
import com.innowise.userservice.infrastructure.persistence.specification.PaymentCardSpecification;
import com.innowise.userservice.infrastructure.profiling.annotation.Profiling;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardApplicationServiceImpl implements PaymentCardApplicationService {

    private final PaymentCardRepository paymentCardRepository;
    private final PaymentCardMapper paymentCardMapper;
    private final EntityManager entityManager;

    @Override
    @CustomCacheable(cacheName = CacheConfig.PAYMENT_CARDS_CACHE, keyArgumentIndexes = {0})
    public PaymentCardResponseDto getPaymentCardById(Long id) throws PaymentCardNotFoundException {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    @CustomCacheable(cacheName = CacheConfig.PAYMENT_CARDS_FILTERED_AND_PAGED_CACHE, keyArgumentIndexes = {0})
    public Page<PaymentCardResponseDto> getAllPaymentCards(PaymentCardFilter filter, Pageable pageable) {
        return paymentCardRepository.findAll(PaymentCardSpecification.fromUserFilter(filter), pageable).map(paymentCardMapper::toDto);
    }

    @Override
    @Transactional
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, PaymentCardNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, PaymentCardNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    @CustomCaching(
            evict = {
                    @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_CACHE, keyArgumentIndexes = {0}),
                    @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_VIA_USER_ID_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_FILTERED_AND_PAGED_CACHE, allEntries = true)
            }
    )
    public void deactivateCardById(Long id) throws PaymentCardNotFoundException {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
        entityManager.lock(paymentCard.getUser(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        paymentCard.setActive(false);
    }

    @Override
    @Transactional
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, PaymentCardNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, PaymentCardNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    @CustomCaching(
            evict = {
                    @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_CACHE, keyArgumentIndexes = {0}),
                    @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_VIA_USER_ID_CACHE, allEntries = true),
                    @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_FILTERED_AND_PAGED_CACHE, allEntries = true)
            }
    )
    public void activateCardById(Long id) throws PaymentCardNotFoundException {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
        entityManager.lock(paymentCard.getUser(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        paymentCard.setActive(true);
    }

    @Override
    @Profiling
    @CustomCacheable(cacheName = CacheConfig.PAYMENT_CARDS_VIA_USER_ID_CACHE, keyArgumentIndexes = {0})
    public List<PaymentCardResponseDto> getCardsByUserId(Long userId) {
        return paymentCardMapper.toDtoList(paymentCardRepository.findByUserId(userId));
    }

    @Override
    @Transactional
    @Retryable(
            noRetryFor = {DataIntegrityViolationException.class, PaymentCardNotFoundException.class},
            notRecoverable = {DataIntegrityViolationException.class, PaymentCardNotFoundException.class},
            retryFor = {OptimisticLockException.class, TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 1.3)
    )
    @CustomCaching(
        evict = {
                @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_CACHE, keyArgumentIndexes = {1}),
                @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_VIA_USER_ID_CACHE, allEntries = true),
                @CustomCacheEvict(cacheName = CacheConfig.PAYMENT_CARDS_FILTERED_AND_PAGED_CACHE, allEntries = true)
        }
    )
    public void updateCard(UpdatePaymentCardDto updatePaymentCardDto, Long id) throws PaymentCardNotFoundException {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
        entityManager.lock(paymentCard.getUser(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        paymentCardMapper.updateEntity(updatePaymentCardDto, paymentCard);
    }

    @Recover
    public void RecoverChangeCardStatus(Throwable e, Long id) throws FailedToPerformOperationException {
        log.error("Failed to change status of a card with id {} after retries", id, e);
        throw new FailedToPerformOperationException("Unable to change status of a card with id: " + id);
    }

    @Recover
    public void RecoverUpdateCard(Throwable e,UpdatePaymentCardDto updatePaymentCardDto, Long id) throws FailedToPerformOperationException {
        log.error("Failed to update card with id {} after retries", id, e);
        throw new FailedToPerformOperationException("Unable to update card with id: " + id);
    }
}
