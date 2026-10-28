package com.iiitd.erp.Domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * A student's results by term. SGPA/CGPA = sum(grade points x credits) / sum(credits) over courses
 * with a final grade (F counts as 0 points); courses still in progress are listed without a grade.
 */
public record AcademicRecordView(String studentName,
                                 String rollNo,
                                 String programId,
                                 List<TermRecord> terms,
                                 int creditsEarned,
                                 int creditsGraded,
                                 Double cgpa) {

    public record TermRecord(String semester, int year, List<CourseRecord> courses, int creditsGraded, Double sgpa) {}

    public record CourseRecord(String courseId, String title, int credits, String grade, Integer gradePoints,
                               BigDecimal total) {}
}
