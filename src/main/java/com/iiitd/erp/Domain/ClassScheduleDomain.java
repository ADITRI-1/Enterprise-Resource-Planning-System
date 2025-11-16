package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@With
public class ClassScheduleDomain {
    private UUID scheduleId;
    private String sectionId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomId;
    private String roomNumber;
    private Integer roomCapacity;
}
