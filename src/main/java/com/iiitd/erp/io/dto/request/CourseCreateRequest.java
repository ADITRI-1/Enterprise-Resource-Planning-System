package com.iiitd.erp.io.dto.request;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record CourseCreateRequest(
        String courseId,
        String title,
        String deptId,
        Integer credits
) {}
