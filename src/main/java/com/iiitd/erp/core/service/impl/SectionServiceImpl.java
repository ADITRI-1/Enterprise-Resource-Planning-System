package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.CourseDomain;
import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.core.CoursePersistPort;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.core.service.support.SemesterTerm;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SectionServiceImpl implements SectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SectionServiceImpl.class);

    private final SectionPersistPort sections;
    private final CoursePersistPort courses;
    private final InstructorPersistPort instructors;

    public SectionServiceImpl(SectionPersistPort sections,
                              CoursePersistPort courses,
                              InstructorPersistPort instructors) {
        this.sections = sections;
        this.courses = courses;
        this.instructors = instructors;
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
                .withSemester(term.label())
                .withYear(sectionDomain.getYear())
                .withCapacity(sectionDomain.getCapacity())
                .withActive(sectionDomain.getActive())
                .withRegistrationDeadline(sectionDomain.getRegistrationDeadline());

        try {
            SectionDomain saved = sections.update(payload);
            return saved.withInstructorCode(instructor.getInstructorCode());
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

    private String normalizeSemester(String semester) {
        return (semester == null || semester.isBlank()) ? null : semester.trim().toUpperCase(Locale.ROOT);
    }

}
