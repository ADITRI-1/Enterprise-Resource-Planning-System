package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Value
@Builder
public class InstructorTimetableSlotDomain {
    String sectionId;
    String courseId;
    String semester;
    Integer year;
    DayOfWeek dayOfWeek;
    LocalTime startTime;
    LocalTime endTime;
    String roomId;
}
