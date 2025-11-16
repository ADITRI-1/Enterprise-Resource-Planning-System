package com.iiitd.erp.io.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ScheduleSlotRequest(
        @NotNull DayOfWeek day,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotBlank String roomId) {}
