package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SectionResponse(
        String sectionId,
        String courseId,
        String instructorCode,
        String semester,
        int year,
        int capacity,
        int enrollmentCount,
        boolean active,
        LocalDate registrationDeadline
) {}
