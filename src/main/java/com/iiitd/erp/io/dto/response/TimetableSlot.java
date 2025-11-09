package com.iiitd.erp.io.dto.response;
public record TimetableSlot(int dayOfWeek,
                            String startTime,
                            String endTime,
                            String courseCode,
                            String courseTitle,
                            String room) {}
