package com.iiitd.erp.io.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record LoginRequest (
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String role
){}