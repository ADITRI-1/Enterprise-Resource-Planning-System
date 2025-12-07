package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.UserAuthPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import com.iiitd.erp.persistent.persistence.repository.UserAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class UserAuthPersistPortImpl implements UserAuthPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthPersistPortImpl.class);

    private final UserAuthRepository userAuthRepository;

    public UserAuthPersistPortImpl(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public Optional<UserAuthDomain> findByUsername(String username) {
        LOGGER.debug("Fetching auth record for {}", username);
        try {
            return userAuthRepository.findByUsername(username)
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::userAuthToDomain);
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch auth record for {}", username, ex);
            throw ErpException.wrap(
                    ErrorCode.USERAUTH_FETCH_FAILED,
                    ex,
                    Collections.singletonMap("username", username));
        }
    }

    @Override
    public UserAuthDomain save(UserAuthDomain userAuthDomain) {
        String username = userAuthDomain.getUsername();
        LOGGER.debug("Persisting auth record for {}", username);
        try {
            UserAuth entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.domainToUserAuthDomain(userAuthDomain);
            UserAuth saved = userAuthRepository.save(entity);
            LOGGER.info("Persisted auth record for {}", username);
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.userAuthToDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist auth record for {}", username, ex);
            throw ErpException.wrap(
                    ErrorCode.USERAUTH_PERSIST_FAILED,
                    ex,
                    Collections.singletonMap("username", username));
        }
    }

    @Override
    public int updateLastLogin(String username, OffsetDateTime loginTime) {
        LOGGER.debug("Updating last login for {}", username);
        try {
            return userAuthRepository.updateLastLogin(username, loginTime);
        } catch (Exception ex) {
            LOGGER.error("Failed to update last login for {}", username, ex);
            throw ErpException.wrap(
                    ErrorCode.USERAUTH_LAST_LOGIN_UPDATE_FAILED,
                    ex,
                    Collections.singletonMap("username", username));
        }
    }

    @Override
    public void deleteByUserId(UUID userId) {
        LOGGER.debug("Deleting auth record {}", userId);
        try {
            userAuthRepository.deleteById(userId);
            LOGGER.info("Deleted auth record {}", userId);
        } catch (Exception ex) {
            LOGGER.error("Failed to delete auth record {}", userId, ex);
            throw ex; // service layer will wrap into its own error code
        }
    }
}
