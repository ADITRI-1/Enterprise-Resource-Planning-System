package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.util.List;

@Data
@Builder
@With
public class InstructorScoreOverviewDomain {
    SectionDomain section;
    List<AssessmentComponentDomain> components;
    List<InstructorStudentScoreDomain> students;
}
