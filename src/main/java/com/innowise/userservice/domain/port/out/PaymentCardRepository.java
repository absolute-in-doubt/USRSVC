package com.innowise.userservice.domain.port.out;

import com.innowise.userservice.domain.model.PaymentCard;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "UPDATE payment_cards SET active = :active WHERE id = :cardId",  nativeQuery = true)
    void setActiveById(@Param("cardId") Long cardId, @Param("active")  boolean active);
}
