package com.innowise.userservice.application.service.impl;

import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.PaymentCardResponseDto;
import com.innowise.userservice.application.mapper.PaymentCardMapper;
import com.innowise.userservice.application.service.PaymentCardApplicationService;
import com.innowise.userservice.domain.model.PaymentCard;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import com.innowise.userservice.domain.port.out.PaymentCardRepository;
import com.innowise.userservice.infrastructure.persistence.specification.PaymentCardSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardApplicationServiceImpl implements PaymentCardApplicationService {

    private final PaymentCardRepository paymentCardRepository;
    private final PaymentCardMapper paymentCardMapper;
    private final EntityManager entityManager;

    @Override
    public PaymentCardResponseDto getPaymentCardById(Long id) throws PaymentCardNotFoundException {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
        return paymentCardMapper.toDto(paymentCard);
    }

    @Override
    public Page<PaymentCard> getAllPaymentCards(PaymentCardFilter filter, Pageable pageable) {
        return paymentCardRepository.findAll(PaymentCardSpecification.fromUserFilter(filter), pageable);
    }

    @Override
    @Transactional
    public void deactivateCardById(Long id) throws PaymentCardNotFoundException {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
        entityManager.lock(paymentCard.getUser(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        paymentCard.setActive(false);
    }

    @Override
    @Transactional
    public void activateCardById(Long id) throws PaymentCardNotFoundException {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new PaymentCardNotFoundException(id));
        entityManager.lock(paymentCard.getUser(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        paymentCard.setActive(false);
    }

    @Override
    public List<PaymentCardResponseDto> getCardsByUserId(Long userId) {
        return paymentCardMapper.toDtoList(paymentCardRepository.findByUserId(userId));
    }
}
