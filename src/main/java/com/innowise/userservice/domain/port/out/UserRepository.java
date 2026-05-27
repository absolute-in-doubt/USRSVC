package com.innowise.userservice.domain.port.out;

import com.innowise.userservice.application.dto.FullUserResponseDto;
import com.innowise.userservice.domain.model.User;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Modifying
    @Query(value = "UPDATE users SET active = :active WHERE id = :userId",  nativeQuery = true)
    void setActiveById(@Param("userId") Long userId, @Param("active")  boolean active);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards WHERE u.id = :userId")
    Optional<User> findByIdWithCards(@Param("userId") Long userId);
}
