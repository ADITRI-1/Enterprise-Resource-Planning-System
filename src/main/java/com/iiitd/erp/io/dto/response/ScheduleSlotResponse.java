package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record ScheduleSlotResponse(
        UUID scheduleId,
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime,
        String roomId,
        String roomNumber,
        Integer roomCapacity) {}
