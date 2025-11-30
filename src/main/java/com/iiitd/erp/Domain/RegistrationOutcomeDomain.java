package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegistrationOutcomeDomain {
    String status; // OK/BLOCKED/ERROR
    String message;
}
