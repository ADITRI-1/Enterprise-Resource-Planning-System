package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.core.*;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.core.service.support.SemesterTerm;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.persistent.persistence.repository.GradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SectionServiceImpl implements SectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SectionServiceImpl.class);

    private final SectionPersistPort sections;
    private final CoursePersistPort courses;
    private final InstructorPersistPort instructors;
    private final EnrollmentPersistPort enrollments;
    private final StudentPersistPort students;
    private final ClassSchedulePersistPort schedules;
    private final RoomPersistPort rooms;
    private final AssessmentComponentPersistPort components;
    private final AssessmentScorePersistPort scores;
    private final GradeRepository grades;

    public SectionServiceImpl(SectionPersistPort sections,
                              CoursePersistPort courses,
                              InstructorPersistPort instructors,
                              EnrollmentPersistPort enrollments,
                              StudentPersistPort students,
                              ClassSchedulePersistPort schedules,
                              RoomPersistPort rooms,
                              AssessmentComponentPersistPort components,
                              AssessmentScorePersistPort scores,
                              GradeRepository grades) {
        this.sections = sections;
        this.courses = courses;
        this.instructors = instructors;
        this.enrollments = enrollments;
        this.students = students;
        this.schedules = schedules;
        this.rooms = rooms;
        this.components = components;
        this.scores = scores;
        this.grades = grades;
    }

    @Override
    public SectionDomain createSection(String courseId, SectionDomain sectionDomain) {
        CourseDomain course = ensureCourse(courseId);
        InstructorDomain instructor = ensureInstructorByCode(sectionDomain.getInstructorCode());
        SemesterTerm term = parseSemester(sectionDomain.getSemester());
        String sectionId = buildSectionId(course.getCourseId(), instructor.getInstructorCode(), sectionDomain.getYear(), term);

        LOGGER.info("Creating section {} for course {}", sectionId, courseId);
        if (sections.findBySectionId(sectionId) != null) {
            throw ErpException.of(ErrorCode.SECTION_DUPLICATE, Map.of("sectionId", sectionId));
        }

        SectionDomain payload = SectionDomain.builder()
                .sectionId(sectionId)
                .courseId(course.getCourseId())
                .instructorId(instructor.getInstructorId())
                .instructorCode(instructor.getInstructorCode())
                .semester(term.label())
                .year(sectionDomain.getYear())
                .capacity(sectionDomain.getCapacity())
                .enrollmentCount(0)
                .active(sectionDomain.getActive() == null ? Boolean.TRUE : sectionDomain.getActive())
                .registrationDeadline(sectionDomain.getRegistrationDeadline())
                .build();

        try {
            SectionDomain saved = sections.save(payload);
            return saved.withInstructorCode(instructor.getInstructorCode());
        } catch (ErpException ex) {
            LOGGER.error("Known failure creating section {}: {}", sectionId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected failure creating section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_CREATE_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public SectionDomain updateSection(String sectionId, SectionDomain sectionDomain) {
        LOGGER.info("Updating section {}", sectionId);
        SectionDomain existing = sections.findBySectionId(sectionId);
        if (existing == null) {
            throw ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", sectionId));
        }

        InstructorDomain instructor = ensureInstructorByCode(sectionDomain.getInstructorCode());
        SemesterTerm term = parseSemester(sectionDomain.getSemester());
        String expectedId = buildSectionId(existing.getCourseId(), instructor.getInstructorCode(), sectionDomain.getYear(), term);
        if (!expectedId.equals(sectionId)) {
            throw ErpException.of(ErrorCode.SECTION_ID_IMMUTABLE,
                    Map.of("sectionId", sectionId, "expectedSectionId", expectedId));
        }

        SectionDomain payload = existing
                .withInstructorId(instructor.getInstructorId())
                .withInstructorCode(instructor.getInstructorCode())
                .withSemester(term.label())
                .withYear(sectionDomain.getYear())
                .withCapacity(sectionDomain.getCapacity())
                .withActive(sectionDomain.getActive())
                .withRegistrationDeadline(sectionDomain.getRegistrationDeadline());

        try {
            return sections.update(payload).withInstructorCode(instructor.getInstructorCode());
        } catch (ErpException ex) {
            LOGGER.error("Known failure updating section {}: {}", sectionId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected failure updating section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_UPDATE_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public boolean deleteSection(String sectionId) {
        LOGGER.info("Deleting section {}", sectionId);
        try {
            return sections.delete(sectionId);
        } catch (ErpException ex) {
            LOGGER.error("Known failure deleting section {}: {}", sectionId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected failure deleting section {}", sectionId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_DELETE_FAILED, ex, Map.of("sectionId", sectionId));
        }
    }

    @Override
    public List<SectionDomain> listSections(String courseId, String semester, Integer year) {
        ensureCourse(courseId);
        SemesterTerm term = (semester == null || semester.isBlank()) ? null : parseSemester(semester);
        String normalizedSemester = term == null ? null : term.label();
        try {
            return sections.findByCourse(courseId, normalizedSemester, year).stream()
                    .map(this::attachInstructorCode)
                    .collect(Collectors.toList());
        } catch (ErpException ex) {
            LOGGER.error("Known failure listing sections for {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected failure listing sections for {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.SECTION_LIST_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    // ---------------- Student-facing operations ----------------

    @Override
    public RegistrationOutcomeDomain registerStudent(String studentUserId, String sectionId) {
        UUID studentId = requireStudent(studentUserId);
        SectionDomain section = requireSection(sectionId);
        LOGGER.info("Student {} attempting registration for section {}", studentUserId, sectionId);

        ensureRegistrationOpen(section);
        ensureNotDuplicate(studentId, sectionId);
        ensureCapacity(sectionId, section.getCapacity());

        enrollments.save(EnrollmentDomain.builder()
                .enrollmentId(UUID.randomUUID())
                .studentId(studentId)
                .sectionId(sectionId)
                .enrollmentDate(OffsetDateTime.now())
                .status("ENROLLED")
                .build());
        bumpSectionCount(sectionId, 1);

        return RegistrationOutcomeDomain.builder().status("OK").message("Registered successfully").build();
    }

    @Override
    public boolean dropStudent(String studentUserId, String sectionId) {
        UUID studentId = requireStudent(studentUserId);
        SectionDomain section = requireSection(sectionId);
        LOGGER.info("Student {} attempting drop for section {}", studentUserId, sectionId);

        ensureDropAllowed(section);

        EnrollmentDomain enrollment = enrollments.findByStudentAndSection(studentId, sectionId)
                .filter(e -> "ENROLLED".equalsIgnoreCase(e.getStatus()))
                .orElseThrow(() -> ErpException.of(ErrorCode.STUDENT_NOT_IN_SECTION,
                        Map.of("sectionId", sectionId, "studentId", studentUserId)));

        enrollments.save(enrollment.toBuilder().status("DROPPED").build());
        bumpSectionCount(sectionId, -1);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentTimeTableSlotDomain> timetableForStudent(String studentUserId, String semester, int year) {
        UUID studentId = requireStudent(studentUserId);
        String normalizedSemester = normalizeSemester(semester);

        List<EnrollmentDomain> active = enrollments.findByStudentAndStatus(studentId, "ENROLLED");
        if (active.isEmpty()) {
            return List.of();
        }

        Map<String, SectionDomain> sectionMap = active.stream()
                .map(EnrollmentDomain::getSectionId)
                .map(sections::findBySectionId)
                .filter(Objects::nonNull)
                .filter(sec -> normalizedSemester.equalsIgnoreCase(sec.getSemester()) && Objects.equals(sec.getYear(), year))
                .collect(Collectors.toMap(SectionDomain::getSectionId, sec -> sec));

        if (sectionMap.isEmpty()) {
            return List.of();
        }

        List<ClassScheduleDomain> slots = schedules.findBySectionIds(new ArrayList<>(sectionMap.keySet()));
        Map<String, CourseDomain> courseCache = new HashMap<>();
        Map<String, RoomDomain> roomCache = new HashMap<>();

        return slots.stream()
                .map(cs -> toStudentTimetableSlot(cs, sectionMap.get(cs.getSectionId()), courseCache, roomCache))
                .sorted(Comparator.comparingInt(StudentTimeTableSlotDomain::getDayOfWeek)
                        .thenComparing(StudentTimeTableSlotDomain::getStartTime))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentGradeDomain> gradesForStudent(String studentUserId) {
        UUID studentId = requireStudent(studentUserId);
        try {
            List<StudentGradeDomain> rows = grades.findByStudent(studentId).stream().toList();
            if (rows.isEmpty()) {
                return buildGradesFromAssessments(studentId);
            }
            return rows;
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch grades for student {}", studentUserId, ex);
            throw ErpException.wrap(ErrorCode.STUDENT_GRADES_FAILED, ex, Map.of("studentId", studentUserId));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Path exportTranscript(String studentUserId) {
        List<StudentGradeDomain> gradeRows = gradesForStudent(studentUserId);
        StringBuilder sb = new StringBuilder();
        sb.append("CourseCode,CourseTitle,Component,Score,FinalGrade\n");
        gradeRows.forEach(row -> sb.append(escape(row.getCourseCode())).append(',')
                .append(escape(row.getCourseTitle())).append(',')
                .append(escape(row.getComponent())).append(',')
                .append(row.getScore()).append(',')
                .append(escape(row.getFinalGrade())).append('\n'));
        try {
            Path file = Files.createTempFile("transcript-" + studentUserId + "-", ".csv");
            Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
            LOGGER.info("Transcript CSV generated for {} at {}", studentUserId, file);
            return file;
        } catch (Exception ex) {
            LOGGER.error("Failed to generate transcript for {}", studentUserId, ex);
            throw ErpException.wrap(ErrorCode.STUDENT_TRANSCRIPT_FAILED, ex, Map.of("studentId", studentUserId));
        }
    }

    // ---------------- helpers ----------------

    private UUID requireStudent(String userId) {
        try {
            UUID uid = UUID.fromString(userId);
            StudentUserDomain student = students.findByStudentId(uid);
            if (student == null) {
                throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("studentId", userId));
            }
            return uid;
        } catch (IllegalArgumentException ex) {
            throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("studentId", userId));
        }
    }

    private SectionDomain requireSection(String sectionId) {
        SectionDomain section = sections.findBySectionId(sectionId);
        if (section == null) {
            throw ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", sectionId));
        }
        return section;
    }

    private void ensureRegistrationOpen(SectionDomain section) {
        LocalDate today = LocalDate.now();
        if (Boolean.FALSE.equals(section.getActive())) {
            throw ErpException.of(ErrorCode.STUDENT_REGISTRATION_BLOCKED, Map.of("sectionId", section.getSectionId()));
        }
        if (section.getRegistrationDeadline() != null && today.isAfter(section.getRegistrationDeadline())) {
            throw ErpException.of(ErrorCode.STUDENT_REGISTRATION_BLOCKED,
                    Map.of("sectionId", section.getSectionId(), "deadline", section.getRegistrationDeadline()));
        }
    }

    private void ensureDropAllowed(SectionDomain section) {
        LocalDate today = LocalDate.now();
        if (section.getRegistrationDeadline() != null && today.isAfter(section.getRegistrationDeadline())) {
            throw ErpException.of(ErrorCode.STUDENT_DROP_FAILED,
                    Map.of("sectionId", section.getSectionId(), "deadline", section.getRegistrationDeadline()));
        }
    }

    private void ensureNotDuplicate(UUID studentId, String sectionId) {
        enrollments.findByStudentAndSection(studentId, sectionId)
                .filter(e -> "ENROLLED".equalsIgnoreCase(e.getStatus()))
                .ifPresent(e -> {
                    throw ErpException.of(ErrorCode.DUPLICATE_STUDENT,
                            Map.of("studentId", studentId, "sectionId", sectionId));
                });
    }

    private void ensureCapacity(String sectionId, Integer capacity) {
        int cap = capacity == null ? 0 : capacity;
        long enrolled = enrollments.countEnrolled(sectionId);
        if (enrolled >= cap) {
            throw ErpException.of(ErrorCode.STUDENT_REGISTRATION_FAILED,
                    Map.of("sectionId", sectionId, "reason", "Section full"));
        }
    }

    private void bumpSectionCount(String sectionId, int delta) {
        SectionDomain section = requireSection(sectionId);
        int current = section.getEnrollmentCount() == null ? 0 : section.getEnrollmentCount();
        int updated = Math.max(0, current + delta);
        sections.update(section.withEnrollmentCount(updated));
    }

    private List<StudentGradeDomain> buildGradesFromAssessments(UUID studentId) {
        List<EnrollmentDomain> completed = enrollments.findByStudentAndStatuses(studentId, List.of("ENROLLED", "COMPLETED"));
        if (completed.isEmpty()) {
            return List.of();
        }
        List<StudentGradeDomain> rows = new ArrayList<>();
        for (EnrollmentDomain enrollment : completed) {
            SectionDomain section = sections.findBySectionId(enrollment.getSectionId());
            if (section == null) continue;
            CourseDomain course = courses.findByCourseId(section.getCourseId());
            String courseCode = course != null ? course.getCourseId() : section.getCourseId();
            String courseTitle = course != null ? course.getTitle() : null;

            Map<UUID, AssessmentComponentDomain> componentMap = components.listBySection(section.getSectionId()).stream()
                    .collect(Collectors.toMap(AssessmentComponentDomain::getComponentId, c -> c));

            List<AssessmentScoreDomain> componentScores = scores.findBySection(section.getSectionId()).stream()
                    .filter(sc -> sc.getEnrollmentId().equals(enrollment.getEnrollmentId()))
                    .toList();

            for (AssessmentScoreDomain score : componentScores) {
                AssessmentComponentDomain comp = componentMap.get(score.getComponentId());
                rows.add(StudentGradeDomain.builder()
                        .courseCode(courseCode)
                        .courseTitle(courseTitle)
                        .component(comp != null ? comp.getName() : "Component")
                        .score(score.getScore())
                        .finalGrade(null)
                        .build());
            }
        }
        return rows;
    }

    private StudentTimeTableSlotDomain toStudentTimetableSlot(ClassScheduleDomain cs,
                                                              SectionDomain section,
                                                              Map<String, CourseDomain> courseCache,
                                                              Map<String, RoomDomain> roomCache) {
        CourseDomain course = courseCache.computeIfAbsent(section.getCourseId(), courses::findByCourseId);
        RoomDomain room = roomCache.computeIfAbsent(cs.getRoomId(), rooms::findByRoomId);
        String courseCode = course != null ? course.getCourseId() : section.getCourseId();
        String courseTitle = course != null ? course.getTitle() : null;
        String roomNumber = room != null ? room.getRoomNumber() : cs.getRoomId();
        return StudentTimeTableSlotDomain.builder()
                .dayOfWeek(cs.getDayOfWeek().getValue())
                .startTime(cs.getStartTime())
                .endTime(cs.getEndTime())
                .courseCode(courseCode)
                .courseTitle(courseTitle)
                .room(roomNumber)
                .build();
    }

    private String normalizeSemester(String semester) {
        return semester == null ? null : semester.trim().toUpperCase(Locale.ROOT);
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\"", "\"\"");
    }

    private CourseDomain ensureCourse(String courseId) {
        CourseDomain course = courses.findByCourseId(courseId);
        if (course == null) {
            throw ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId));
        }
        return course;
    }

    private InstructorDomain ensureInstructorByCode(String instructorCode) {
        InstructorDomain instructor = instructors.findByInstructorCode(instructorCode);
        if (instructor == null) {
            throw ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND, Map.of("instructorCode", instructorCode));
        }
        return instructor;
    }

    private SectionDomain attachInstructorCode(SectionDomain section) {
        if (section.getInstructorId() == null) {
            return section;
        }
        InstructorDomain instructor = instructors.findByInstructorCode(section.getInstructorCode());
        String code = instructor != null ? instructor.getInstructorCode() : null;
        return section.withInstructorCode(code);
    }

    private SemesterTerm parseSemester(String semester) {
        try {
            return SemesterTerm.from(semester);
        } catch (IllegalArgumentException ex) {
            throw ErpException.of(ErrorCode.SECTION_INVALID_SEMESTER, Map.of("semester", semester));
        }
    }

    private String buildSectionId(String courseId, String instructorCode, int year, SemesterTerm term) {
        String yearSuffix = String.format("%02d", Math.floorMod(year, 100));
        return "%s-%s-%s-%s".formatted(
                courseId.toUpperCase(Locale.ROOT),
                instructorCode.toUpperCase(Locale.ROOT),
                yearSuffix,
                term.marker());
    }

    @Override
    public List<SectionDomain> sectionsForInstructor(String instructorCode, String semester, Integer year) {
        ensureInstructorExists(instructorCode);
        String normalizedSemester = normalizeSemester(semester);
        LOGGER.info("Listing sections for instructor {} (semester={}, year={})",
                instructorCode, normalizedSemester, year);
        return sections.findByInstructor(instructorCode, normalizedSemester, year);
    }

    private void ensureInstructorExists(String instructorCode) {
        if (instructors.findByInstructorCode(instructorCode) == null) {
            throw ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND, Map.of("instructorCode", instructorCode));
        }
    }
}
