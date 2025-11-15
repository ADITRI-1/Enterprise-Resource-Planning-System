package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.io.dto.request.SectionCreateRequest;
import com.iiitd.erp.io.dto.request.SectionUpdateRequest;

import java.util.List;

public interface SectionService {
    SectionDomain createSection(String courseId, SectionCreateRequest request);
    SectionDomain updateSection(String sectionId, SectionUpdateRequest request);
    boolean deleteSection(String sectionId);
    List<SectionDomain> listSections(String courseId, String semester, Integer year);
}