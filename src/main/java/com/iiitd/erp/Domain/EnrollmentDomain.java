package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class EnrollmentDomain {
    public static final String ENROLLED = "enrolled";
    public static final String DROPPED = "dropped";
    public static final String COMPLETED = "completed";

    UUID enrollmentId;
    UUID studentId;
    String sectionId;
    OffsetDateTime enrollmentDate;
    String status;
}
