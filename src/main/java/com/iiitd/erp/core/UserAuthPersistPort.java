package com.iiitd.erp.core;

import com.iiitd.erp.Domain.UserAuthDomain;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserAuthPersistPort {
    Optional<UserAuthDomain> findByUsername(String username);
    UserAuthDomain save(UserAuthDomain userAuthDomain);
    int updateLastLogin(String username, OffsetDateTime loginTime);
    void deleteByUserId(UUID userId);
}
