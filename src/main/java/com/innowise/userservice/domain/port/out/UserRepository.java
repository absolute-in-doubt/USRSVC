package com.innowise.userservice.domain.port.out;

import com.innowise.userservice.domain.model.User;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Modifying
    @Query(value = "UPDATE users SET active = :active WHERE id = :userId",  nativeQuery = true)
    void setActiveById(@Param("userId") Long userId, @Param("active")  boolean active);
}
