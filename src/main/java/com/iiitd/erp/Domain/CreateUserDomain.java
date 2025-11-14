package com.iiitd.erp.Domain;

import com.iiitd.erp.io.dto.AccountStatus;
import com.iiitd.erp.io.dto.Role;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.util.UUID;

@Data
@Builder
@With
public class CreateUserDomain {
    String username;
    Role role;
    AccountStatus status;
}
