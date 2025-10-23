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
    Long id,
    String username,
    Role role,
    AccountStatus status
){}
