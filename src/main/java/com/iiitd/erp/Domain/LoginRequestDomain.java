package com.iiitd.erp.Domain;


import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@With
public class LoginRequestDomain {
    private String username;
    private String password;
}