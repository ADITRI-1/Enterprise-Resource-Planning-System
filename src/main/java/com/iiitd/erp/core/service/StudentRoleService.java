package com.iiitd.erp.core.service;

import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.io.dto.response.RegistrationResult;
import com.iiitd.erp.io.dto.response.TimetableSlot;

import java.nio.file.Path;
import java.util.List;

public interface StudentRoleService {
    RegistrationResult register(long studentUserId, long sectionId);
    boolean drop(long studentUserId, long sectionId);
    List<TimetableSlot> myTimetable(long studentUserId, String semester, int year);
    List<GradeRow> myGrades(long studentUserId);
    Path exportTranscriptCsv(long studentUserId);
}
