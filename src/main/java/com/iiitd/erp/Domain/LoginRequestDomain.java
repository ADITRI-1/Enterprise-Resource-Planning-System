package com.iiitd.erp.Domain;


import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.With;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDomain {
    private String username;
    private String password;
}
