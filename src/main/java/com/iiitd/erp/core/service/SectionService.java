package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.io.dto.response.RegistrationResult;
import com.iiitd.erp.io.dto.response.TimetableSlot;

import java.nio.file.Path;
import java.util.List;

public interface SectionService {
    SectionDomain createSection(String courseId, SectionDomain sectionDomain);
    SectionDomain updateSection(String sectionId, SectionDomain sectionDomain);
    boolean deleteSection(String sectionId);
    List<SectionDomain> listSections(String courseId, String semester, Integer year);
    List<SectionDomain> sectionsForInstructor(String instructorCode, String semester, Integer year);

    // Student-facing operations
    RegistrationResult registerStudent(String studentUserId, String sectionId);
    boolean dropStudent(String studentUserId, String sectionId);
    List<TimetableSlot> timetableForStudent(String studentUserId, String semester, int year);
    List<GradeRow> gradesForStudent(String studentUserId);
    Path exportTranscript(String studentUserId);
}
