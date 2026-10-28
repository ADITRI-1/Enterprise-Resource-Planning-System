package com.iiitd.erp.Domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Admin analytics for one term (or all terms when none is given). */
public record AnalyticsView(List<SectionFill> sectionFill,
                            List<DepartmentCount> enrollmentsByDepartment,
                            Map<String, Long> gradeDistribution,
                            List<DailyCount> registrationsByDay) {

    public record SectionFill(String sectionId, String courseId, int enrolled, int capacity) {}

    public record DepartmentCount(String deptId, long enrolled) {}

    public record DailyCount(LocalDate date, long registrations) {}
}
