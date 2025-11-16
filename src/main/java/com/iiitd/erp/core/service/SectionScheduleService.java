package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.ClassScheduleDomain;
import com.iiitd.erp.core.service.dto.SectionScheduleResult;

import java.util.List;

public interface SectionScheduleService {
    SectionScheduleResult view(String sectionId);
    SectionScheduleResult replace(String sectionId, List<ClassScheduleDomain> slots);
    void clear(String sectionId);
}
