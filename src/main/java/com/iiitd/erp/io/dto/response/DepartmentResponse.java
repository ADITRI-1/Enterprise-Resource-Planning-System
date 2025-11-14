package com.iiitd.erp.io.dto.response;

import lombok.Builder;

@Builder
public record DepartmentResponse(
        String deptId,
        String deptDesc
) {}
