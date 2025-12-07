package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.RegistrationOutcomeDomain;
import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.Domain.StudentGradeDomain;
import com.iiitd.erp.Domain.StudentTimeTableSlotDomain;

import java.nio.file.Path;
import java.util.List;

public interface SectionService {
    SectionDomain createSection(String courseId, SectionDomain sectionDomain);
    SectionDomain updateSection(String sectionId, SectionDomain sectionDomain);
    boolean deleteSection(String sectionId);
    List<SectionDomain> listSections(String courseId, String semester, Integer year);
    List<SectionDomain> sectionsForInstructor(String instructorCode, String semester, Integer year);
    List<SectionDomain> listAllSections(String semester, Integer year);

    // Student-facing operations
    RegistrationOutcomeDomain registerStudent(String studentUserId, String sectionId);
    boolean dropStudent(String studentUserId, String sectionId);
    List<StudentTimeTableSlotDomain> timetableForStudent(String studentUserId, String semester, int year);
    List<StudentGradeDomain> gradesForStudent(String studentUserId);
    Path exportTranscript(String studentUserId);
}
