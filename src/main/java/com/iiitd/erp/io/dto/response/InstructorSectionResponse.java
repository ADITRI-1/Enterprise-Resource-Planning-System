package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InstructorSectionResponse(
        String sectionId,
        String courseId,
        String semester,
        int year,
        int capacity,
        int enrollmentCount,
        boolean active,
        LocalDate registrationDeadline) {}
