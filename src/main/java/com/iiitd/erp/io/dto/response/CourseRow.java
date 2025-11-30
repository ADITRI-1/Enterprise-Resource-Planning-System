package com.iiitd.erp.io.dto.response;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record CourseRow(String courseId,
                        String title,
                        int credits) {}
