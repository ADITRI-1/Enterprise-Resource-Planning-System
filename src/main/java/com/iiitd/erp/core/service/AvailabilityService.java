package com.iiitd.erp.core.service;

import com.iiitd.erp.io.dto.response.InstructorAvailabilityResponse;
import com.iiitd.erp.io.dto.response.RoomAvailabilityResponse;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {
    List<RoomAvailabilityResponse> availableRooms(DayOfWeek day,
                                                  LocalTime start,
                                                  LocalTime end,
                                                  int minCapacity);

    InstructorAvailabilityResponse instructorAvailability(String instructorCode,
                                                          String semester,
                                                          int year);

    void ensureSlotFree(String instructorCode,
                        String sectionIdToIgnore,
                        String roomId,
                        DayOfWeek day,
                        LocalTime start,
                        LocalTime end);
}
