package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.CourseDomain;
import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.core.CoursePersistPort;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.service.CatalogService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.response.CourseRow;
import com.iiitd.erp.io.dto.response.SectionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final CoursePersistPort courses;
    private final SectionPersistPort sections;
    private final InstructorPersistPort instructors;

    public CatalogServiceImpl(CoursePersistPort courses,
                              SectionPersistPort sections,
                              InstructorPersistPort instructors) {
        this.courses = courses;
        this.sections = sections;
        this.instructors = instructors;
    }

    @Override
    public List<CourseRow> listCourses(String semester, int year, String q) {
        String query = q == null ? "" : q.trim();
        LOGGER.info("Listing catalog courses for semester={} year={} query='{}'", semester, year, query);
        try {
            return courses.findAll().stream()
                    .filter(course -> matchesQuery(course, query))
                    .map(course -> CourseRow.builder()
                            .courseId(course.getCourseId())
                            .title(course.getTitle())
                            .credits(defaultInt(course.getCredits()))
                            .build())
                    .toList();
        } catch (ErpException ex) {
            LOGGER.error("Catalog course list failed: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected catalog course list failure", ex);
            throw ErpException.wrap(ErrorCode.CATALOG_LIST_FAILED, ex,
                    Map.of("semester", semester, "year", year));
        }
    }

    @Override
    public List<SectionRow> listSections(String courseId, String semester, int year) {
        String normalizedSemester = normalizeSemester(semester);
        LOGGER.info("Listing catalog sections for course {} semester={} year={}", courseId, normalizedSemester, year);
        ensureCourseExists(courseId);
        try {
            return sections.findByCourse(courseId, normalizedSemester, year).stream()
                    .map(section -> toSectionRow(section, normalizedSemester))
                    .collect(Collectors.toList());
        } catch (ErpException ex) {
            LOGGER.error("Catalog sections failed for course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected catalog sections failure for course {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.CATALOG_SECTIONS_FAILED, ex,
                    Map.of("courseId", courseId, "semester", normalizedSemester, "year", year));
        }
    }

    private boolean matchesQuery(CourseDomain course, String q) {
        if (q.isBlank()) {
            return true;
        }
        String needle = q.toLowerCase(Locale.ROOT);
        return (course.getCourseId() != null && course.getCourseId().toLowerCase(Locale.ROOT).contains(needle))
                || (course.getTitle() != null && course.getTitle().toLowerCase(Locale.ROOT).contains(needle));
    }

    private SectionRow toSectionRow(SectionDomain section, String normalizedSemester) {
        int capacity = defaultInt(section.getCapacity());
        int enrolled = defaultInt(section.getEnrollmentCount());
        int available = Math.max(0, capacity - enrolled);
        String instructorCode = section.getInstructorCode();
        String instructorName = instructors.findByInstructorCode(instructorCode) != null
                ? Objects.requireNonNull(instructors.findByInstructorCode(instructorCode)).getName()
                : null;

        return SectionRow.builder()
                .sectionId(section.getSectionId())
                .courseId(section.getCourseId())
                .instructorCode(instructorCode)
                .instructorName(instructorName)
                .capacity(capacity)
                .enrolled(enrolled)
                .available(available)
                .semester(normalizedSemester != null ? normalizedSemester : section.getSemester())
                .year(section.getYear() == null ? 0 : section.getYear())
                .build();
    }

    private void ensureCourseExists(String courseId) {
        if (courses.findByCourseId(courseId) == null) {
            throw ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId));
        }
    }

    private String normalizeSemester(String semester) {
        return semester == null ? null : semester.trim().toUpperCase(Locale.ROOT);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
