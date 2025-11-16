package com.iiitd.erp.core;

import com.iiitd.erp.Domain.ClassScheduleDomain;

import java.util.List;

public interface ClassSchedulePersistPort {
    List<ClassScheduleDomain> findBySectionId(String sectionId);
    List<ClassScheduleDomain> replaceSectionSchedule(String sectionId, List<ClassScheduleDomain> slots);
    void deleteBySectionId(String sectionId);
}
