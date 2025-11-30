package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class EnrollmentDomain {
    UUID enrollmentId;
    UUID studentId;
    String sectionId;
    OffsetDateTime enrollmentDate;
    String status;
}
