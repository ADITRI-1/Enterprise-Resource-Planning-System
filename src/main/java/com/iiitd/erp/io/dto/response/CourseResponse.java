package com.iiitd.erp.io.dto.response;

import lombok.Builder;

@Builder
public record CourseResponse(
        String courseId,
        String title,
        String deptId,
        Integer credits
) {}
