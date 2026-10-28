package com.iiitd.erp.core.service.support;

import java.util.Optional;

/** Rules a user-chosen password must satisfy. */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    private PasswordPolicy() {}

    /** Returns the reason the password is rejected, or empty if it is acceptable. */
    public static Optional<String> violation(String password, String username) {
        if (password == null || password.length() < MIN_LENGTH) {
            return Optional.of("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (password.chars().noneMatch(Character::isLetter) || password.chars().noneMatch(Character::isDigit)) {
            return Optional.of("Password must contain at least one letter and one digit");
        }
        if (username != null && password.toLowerCase().contains(username.toLowerCase())) {
            return Optional.of("Password must not contain your username");
        }
        return Optional.empty();
    }
}
