package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.util.UUID;

@Data
@Builder
@With
public class StudentUserDomain {
    private UUID studentId;
    private String name;
    private String rollNo;
    private String dateOfBirth;
    private String programId;
    private String yearOfRegistration;
}
