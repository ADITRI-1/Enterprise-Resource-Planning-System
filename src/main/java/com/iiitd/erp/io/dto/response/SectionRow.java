package com.iiitd.erp.io.dto.response;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record SectionRow(String sectionId,
                         String courseId,
                         String instructorCode,
                         String instructorName,
                         int capacity,
                         int enrolled,
                         int available,
                         String semester,
                         int year) {}
