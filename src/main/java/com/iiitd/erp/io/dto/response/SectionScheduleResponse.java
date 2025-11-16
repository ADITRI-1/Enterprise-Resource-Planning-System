package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SectionScheduleResponse(
        String sectionId,
        String courseId,
        List<ScheduleSlotResponse> slots) {}
