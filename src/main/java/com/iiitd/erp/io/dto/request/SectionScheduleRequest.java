package com.iiitd.erp.io.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SectionScheduleRequest(
        @NotEmpty List<@Valid ScheduleSlotRequest> slots) {}
