package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.UserAuthDomain;

import java.util.List;

/** Password and lockout management for login accounts (Auth DB). */
public interface AccountService {

    int MAX_FAILED_ATTEMPTS = 5;
    int LOCK_MINUTES = 15;

    /** Counts a wrong password; commits even though the login itself fails. Returns the updated account. */
    UserAuthDomain recordFailedLogin(String username);

    void recordSuccessfulLogin(String username);

    void changePassword(String username, String currentPassword, String newPassword);

    /** Sets a new temporary password (returned once) and requires a change on next login. */
    String resetPassword(String username);

    void unlock(String username);

    List<UserAuthDomain> listAccounts();
}
