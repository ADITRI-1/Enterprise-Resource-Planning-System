package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.InstructorTimetableSlotDomain;

import java.util.List;

public interface TimetableService {
    List<InstructorTimetableSlotDomain> timetable(String instructorCode, String semester, Integer year);
}
