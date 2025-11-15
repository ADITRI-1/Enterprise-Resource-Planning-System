package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.time.LocalDate;

@Data
@Builder
@With
public class SectionDomain {
    private String sectionId;
    private String courseId;
    private String instructorId;
    private String instructorCode;
    private String semester;
    private Integer year;
    private Integer capacity;
    private Integer enrollmentCount;
    private Boolean active;
    private LocalDate registrationDeadline;
}
