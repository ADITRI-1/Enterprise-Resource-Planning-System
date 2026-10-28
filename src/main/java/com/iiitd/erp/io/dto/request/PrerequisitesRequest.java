package com.iiitd.erp.io.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PrerequisitesRequest(@NotNull List<String> prerequisites) {}
