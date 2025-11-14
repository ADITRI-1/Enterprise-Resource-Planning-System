package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@Builder
@With
public class CourseDomain {
    private String courseId;
    private String title;
    private String deptId;
    private Integer credits;
}
