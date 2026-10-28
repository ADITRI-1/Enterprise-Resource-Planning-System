package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.CatalogSectionView;
import com.iiitd.erp.Domain.CourseDomain;

import java.util.List;

public interface CatalogService {
    List<CourseDomain> listCourses();

    /** Active sections with seats, instructor and schedule. All filters are optional. */
    List<CatalogSectionView> catalogSections(String semester, Integer year, String courseId);
}
