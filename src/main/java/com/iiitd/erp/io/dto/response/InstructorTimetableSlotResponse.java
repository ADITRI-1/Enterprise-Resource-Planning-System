package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
public record InstructorTimetableSlotResponse(
        String sectionId,
        String courseId,
        String semester,
        int year,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String roomId) {}
