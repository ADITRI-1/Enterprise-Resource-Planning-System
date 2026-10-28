package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.Domain.AcademicRecordView.CourseRecord;
import com.iiitd.erp.Domain.AcademicRecordView.TermRecord;
import com.iiitd.erp.core.CoursePersistPort;
import com.iiitd.erp.core.EnrollmentPersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.StudentPersistPort;
import com.iiitd.erp.core.service.AcademicRecordService;
import com.iiitd.erp.core.service.support.GradeScale;
import com.iiitd.erp.core.service.support.TranscriptPdf;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.entity.erp_db.Grade;
import com.iiitd.erp.persistent.persistence.repository.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AcademicRecordServiceImpl implements AcademicRecordService {

    private final StudentPersistPort students;
    private final EnrollmentPersistPort enrollments;
    private final SectionPersistPort sections;
    private final CoursePersistPort courses;
    private final GradeRepository grades;

    public AcademicRecordServiceImpl(StudentPersistPort students,
                                     EnrollmentPersistPort enrollments,
                                     SectionPersistPort sections,
                                     CoursePersistPort courses,
                                     GradeRepository grades) {
        this.students = students;
        this.enrollments = enrollments;
        this.sections = sections;
        this.courses = courses;
        this.grades = grades;
    }

    @Override
    public AcademicRecordView record(String studentUserId) {
        UUID studentId = parse(studentUserId);
        StudentUserDomain student = students.findByStudentId(studentId);
        if (student == null) {
            throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("studentId", studentUserId));
        }
        List<EnrollmentDomain> taken = enrollments.findByStudentAndStatuses(studentId,
                List.of(EnrollmentDomain.ENROLLED, EnrollmentDomain.COMPLETED));
        Map<UUID, Grade> finals = grades.findByEnrollmentIdInAndComponent(
                        taken.stream().map(EnrollmentDomain::getEnrollmentId).toList(), ScoreServiceImpl.FINAL_COMPONENT)
                .stream().collect(Collectors.toMap(Grade::getEnrollmentId, g -> g));

        // Group by term; Winter (Jan-Jun) comes before Monsoon (Jul-Dec) within a year.
        Map<String, List<CourseRecord>> byTerm = new TreeMap<>();
        Map<String, SectionDomain> termSection = new HashMap<>();
        for (EnrollmentDomain e : taken) {
            SectionDomain section = sections.findBySectionId(e.getSectionId());
            if (section == null) continue;
            String key = section.getYear() + ("WINTER".equals(section.getSemester()) ? "-1" : "-2");
            termSection.putIfAbsent(key, section);
            CourseDomain course = courses.findByCourseId(section.getCourseId());
            Grade g = finals.get(e.getEnrollmentId());
            String letter = g == null ? null : g.getFinalGrade();
            byTerm.computeIfAbsent(key, k -> new ArrayList<>()).add(new CourseRecord(
                    section.getCourseId(),
                    course != null ? course.getTitle() : null,
                    course != null && course.getCredits() != null ? course.getCredits() : 0,
                    letter,
                    letter == null ? null : GradeScale.pointsFor(letter),
                    g == null ? null : g.getScore()));
        }

        List<TermRecord> terms = new ArrayList<>();
        int allCredits = 0, earned = 0, allPoints = 0;
        for (Map.Entry<String, List<CourseRecord>> entry : byTerm.entrySet()) {
            List<CourseRecord> rows = entry.getValue().stream().sorted(Comparator.comparing(CourseRecord::courseId)).toList();
            int credits = 0, points = 0;
            for (CourseRecord c : rows) {
                if (c.grade() == null) continue;
                credits += c.credits();
                points += c.credits() * c.gradePoints();
                if (!"F".equals(c.grade())) earned += c.credits();
            }
            allCredits += credits;
            allPoints += points;
            SectionDomain s = termSection.get(entry.getKey());
            terms.add(new TermRecord(s.getSemester(), s.getYear(), rows, credits, gpa(points, credits)));
        }
        return new AcademicRecordView(student.getName(), student.getRollNo(), student.getProgramId(), terms,
                earned, allCredits, gpa(allPoints, allCredits));
    }

    @Override
    public byte[] transcriptPdf(String studentUserId) {
        return TranscriptPdf.render(record(studentUserId));
    }

    private static Double gpa(int points, int credits) {
        return credits == 0 ? null
                : BigDecimal.valueOf(points).divide(BigDecimal.valueOf(credits), 2, RoundingMode.HALF_UP).doubleValue();
    }

    private static UUID parse(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("studentId", id));
        }
    }
}
