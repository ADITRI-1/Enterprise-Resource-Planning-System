package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
public record TimetableSearchSlotResponse(
        String sectionId,
        String courseId,
        String instructorCode,
        String semester,
        Integer year,
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime,
        String roomId) {}
