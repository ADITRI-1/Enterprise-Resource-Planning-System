package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
public class TimetableSearchSlotDomain {
    private String sectionId;
    private String courseId;
    private String instructorCode;
    private String semester;
    private Integer year;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomId;
}
