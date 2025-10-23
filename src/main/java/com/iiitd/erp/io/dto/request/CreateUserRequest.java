package com.iiitd.erp.io.dto.request;


import com.iiitd.erp.io.dto.AccountStatus;
import com.iiitd.erp.io.dto.Role;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record CreateUserRequest (
    String username,
    String password, // raw password from admin; will be hashed
    Role role,
    AccountStatus status
){}
