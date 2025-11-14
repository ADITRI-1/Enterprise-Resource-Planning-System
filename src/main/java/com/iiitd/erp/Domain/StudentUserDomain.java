package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@Builder
@With
public class StudentUserDomain {
    private String name;
    private Integer rollNo;
    private String dateOfBirth;
    private String programId;
    private String yearOfRegistration;
}
