package com.iiitd.erp.io.dto.request;

import jakarta.validation.constraints.NotNull;

public record MaintenanceToggleRequest(@NotNull Boolean maintenanceOn) {}
