package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@Builder
@With
public class DepartmentDomain {
    private String deptId;
    private String deptDesc;
}
