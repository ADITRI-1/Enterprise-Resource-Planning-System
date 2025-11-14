package com.iiitd.erp.core;

import com.iiitd.erp.Domain.CourseDomain;

import java.util.List;

public interface CoursePersistPort {
    CourseDomain findByCourseId(String courseId);
    CourseDomain save(CourseDomain courseDomain);
    CourseDomain update(CourseDomain courseDomain);
    boolean deleteByCourseId(String courseId);
    List<CourseDomain> findAll();
}
