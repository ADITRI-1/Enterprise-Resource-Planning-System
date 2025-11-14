package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, UUID> {
    Optional<UserAuth> findByUsername(String username);

    @Modifying
    @Query(value = "UPDATE auth_db.user_auth SET last_login = :ts WHERE username = :user", nativeQuery = true)
    int updateLastLogin(@Param("user") String username, @Param("ts") OffsetDateTime dateTime);
}