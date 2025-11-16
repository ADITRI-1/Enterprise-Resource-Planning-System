package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;


@Builder
public record InstructorAvailabilityResponse(String instructorCode,
                                             Map<DayOfWeek, List<BusySlot>> busy) {
    public record BusySlot(LocalTime start, LocalTime end, String courseId, String sectionId) {}
}