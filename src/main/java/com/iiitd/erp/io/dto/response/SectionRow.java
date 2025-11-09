package com.iiitd.erp.io.dto.response;
public record SectionRow(long id,
                         long courseId,
                         String instructor,
                         int capacity,
                         int enrolled,
//                         int dayOfWeek,
//                         String startTime,
//                         String endTime,
//                         String room,
                         String semester,
                         int year) {}
