package com.iiitd.erp.core;

import com.iiitd.erp.Domain.SectionDomain;

import java.util.List;

public interface SectionPersistPort {
    SectionDomain findBySectionId(String sectionId);
    // Row-locks the section until the surrounding transaction ends (null if missing).
    SectionDomain lockForUpdate(String sectionId);
    SectionDomain save(SectionDomain sectionDomain);
    SectionDomain update(SectionDomain sectionDomain);
    boolean delete(String sectionId);
    List<SectionDomain> findByCourse(String courseId, String semester, Integer year);
    List<SectionDomain> findByInstructor(String instructorCode, String semester, Integer year);
    List<SectionDomain> findAll(String semester, Integer year);
}
