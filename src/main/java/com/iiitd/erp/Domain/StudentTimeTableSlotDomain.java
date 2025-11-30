package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalTime;

@Value
@Builder
public class StudentTimetableSlotDomain {
    int dayOfWeek;
    LocalTime startTime;
    LocalTime endTime;
    String courseCode;
    String courseTitle;
    String room;
}
