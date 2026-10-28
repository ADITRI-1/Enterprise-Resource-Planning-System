package com.iiitd.erp.io.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Body for student register / drop. */
public record SectionIdRequest(@NotBlank String sectionId) {}
