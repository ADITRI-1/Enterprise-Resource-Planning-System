package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@Builder
@With
public class InstructorDomain {
    private String name;
    private String instructorCode;
    private String dateOfBirth;
    private String department;
    private String email;
    private String phone;
}
