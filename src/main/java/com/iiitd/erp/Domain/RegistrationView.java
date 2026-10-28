package com.iiitd.erp.Domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A student's enrollment in a section, including dropped ones. */
public record RegistrationView(UUID enrollmentId,
                               String sectionId,
                               String courseId,
                               String courseTitle,
                               int credits,
                               String instructorCode,
                               String instructorName,
                               String semester,
                               int year,
                               String status,
                               OffsetDateTime enrolledOn,
                               LocalDate registrationDeadline,
                               boolean canDrop,
                               String finalGrade) {}
