package com.iiitd.erp.core.service;

import com.iiitd.erp.io.dto.response.CourseRow;
import com.iiitd.erp.io.dto.response.SectionRow;

import java.util.List;

public interface CatalogService {
    List<CourseRow> listCourses(String semester, int year, String q);
    List<SectionRow> listSections(String courseId, String semester, int year);
}
