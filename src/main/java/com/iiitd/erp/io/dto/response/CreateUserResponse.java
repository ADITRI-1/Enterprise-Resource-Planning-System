package com.iiitd.erp.io.dto.response;


import com.iiitd.erp.io.dto.AccountStatus;
import com.iiitd.erp.io.dto.Role;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With

public record CreateUserResponse (
    String username,
    Role role,
    AccountStatus status,
    // Shown once to the admin; the user must replace it on first login.
    String temporaryPassword
){}
