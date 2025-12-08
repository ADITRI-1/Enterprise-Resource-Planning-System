package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.InstructorTimetableSlotDomain;
import com.iiitd.erp.Domain.TimetableSearchSlotDomain;

import java.time.DayOfWeek;

import java.util.List;

public interface TimetableService {
    List<InstructorTimetableSlotDomain> timetable(String instructorCode, String semester, Integer year);

    List<TimetableSearchSlotDomain> search(String instructorCode,
                                           String courseId,
                                           String sectionId,
                                           String roomId,
                                           String semester,
                                           Integer year,
                                           DayOfWeek day);
}
