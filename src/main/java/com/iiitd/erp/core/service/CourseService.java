package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.CourseDomain;

import java.util.List;

public interface CourseService {
    CourseDomain createCourse(CourseDomain courseDomain);
    CourseDomain updateCourse(CourseDomain courseDomain);
    boolean deleteCourse(String courseId);
    List<CourseDomain> listCourses();
}
