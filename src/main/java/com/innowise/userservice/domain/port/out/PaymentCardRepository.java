package com.innowise.userservice.domain.port.out;

import com.innowise.userservice.domain.model.PaymentCard;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> findByUserId(@Param("userId") Long userId);

//    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
//    @NullMarked PaymentCard save(PaymentCard paymentCard);
}
