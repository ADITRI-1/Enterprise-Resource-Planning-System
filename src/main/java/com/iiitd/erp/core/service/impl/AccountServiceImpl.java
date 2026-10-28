package com.iiitd.erp.core.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.core.service.AccountService;
import com.iiitd.erp.core.service.support.PasswordPolicy;
import com.iiitd.erp.core.service.support.TemporaryPasswords;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import com.iiitd.erp.persistent.persistence.repository.UserAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);
    private static final int HISTORY_SIZE = 3;

    private final UserAuthRepository users;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public AccountServiceImpl(UserAuthRepository users, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserAuthDomain recordFailedLogin(String username) {
        UserAuth user = users.findByUsernameForUpdate(username).orElse(null);
        if (user == null) {
            return null;
        }
        int attempts = user.getFailedAttempts() + 1;
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(OffsetDateTime.now().plusMinutes(LOCK_MINUTES));
            user.setFailedAttempts(0);
            LOGGER.warn("Account {} locked after {} failed login attempts", username, attempts);
        } else {
            user.setFailedAttempts(attempts);
        }
        return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.userAuthToDomain(users.save(user));
    }

    @Override
    public void recordSuccessfulLogin(String username) {
        users.findByUsername(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            user.setLastLogin(OffsetDateTime.now());
        });
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        UserAuth user = require(username);
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw ErpException.of(ErrorCode.CURRENT_PASSWORD_INCORRECT, Map.of());
        }
        PasswordPolicy.violation(newPassword, username).ifPresent(reason -> {
            throw ErpException.of(ErrorCode.PASSWORD_POLICY_VIOLATION, Map.of("reason", reason));
        });

        List<String> history = readHistory(user);
        boolean reused = passwordEncoder.matches(newPassword, user.getPasswordHash())
                || history.stream().anyMatch(old -> passwordEncoder.matches(newPassword, old));
        if (reused) {
            throw ErpException.of(ErrorCode.PASSWORD_REUSED, Map.of("historySize", HISTORY_SIZE));
        }

        history.add(0, user.getPasswordHash());
        writeHistory(user, history.subList(0, Math.min(HISTORY_SIZE, history.size())));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        LOGGER.info("Password changed for {}", username);
    }

    @Override
    public String resetPassword(String username) {
        UserAuth user = require(username);
        String temporary = TemporaryPasswords.generate();
        user.setPasswordHash(passwordEncoder.encode(temporary));
        user.setMustChangePassword(true);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        LOGGER.info("Password reset for {}", username);
        return temporary;
    }

    @Override
    public void unlock(String username) {
        UserAuth user = require(username);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        LOGGER.info("Account {} unlocked", username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAuthDomain> listAccounts() {
        return users.findAll().stream()
                .sorted(Comparator.comparing(UserAuth::getUsername))
                .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::userAuthToDomain)
                .toList();
    }

    private UserAuth require(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> ErpException.of(ErrorCode.USER_NOT_FOUND, Map.of("username", username)));
    }

    private List<String> readHistory(UserAuth user) {
        if (user.getPasswordHistory() == null || user.getPasswordHistory().isBlank()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(objectMapper.readValue(user.getPasswordHistory(), new TypeReference<List<String>>() {}));
        } catch (Exception ex) {
            LOGGER.warn("Ignoring unreadable password history for {}", user.getUsername(), ex);
            return new ArrayList<>();
        }
    }

    private void writeHistory(UserAuth user, List<String> hashes) {
        try {
            user.setPasswordHistory(objectMapper.writeValueAsString(hashes));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to store password history", ex);
        }
    }
}
