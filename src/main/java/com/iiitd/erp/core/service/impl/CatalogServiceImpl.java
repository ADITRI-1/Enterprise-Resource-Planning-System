package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.core.ClassSchedulePersistPort;
import com.iiitd.erp.core.CoursePersistPort;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.service.CatalogService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final CoursePersistPort courses;
    private final SectionPersistPort sections;
    private final InstructorPersistPort instructors;
    private final ClassSchedulePersistPort schedules;

    public CatalogServiceImpl(CoursePersistPort courses,
                              SectionPersistPort sections,
                              InstructorPersistPort instructors,
                              ClassSchedulePersistPort schedules) {
        this.courses = courses;
        this.sections = sections;
        this.instructors = instructors;
        this.schedules = schedules;
    }

    @Override
    public List<CourseDomain> listCourses() {
        return courses.findAll();
    }

    @Override
    public List<CatalogSectionView> catalogSections(String semester, Integer year, String courseId) {
        String normalizedSemester = semester == null || semester.isBlank() ? null : semester.trim().toUpperCase(Locale.ROOT);
        if (courseId != null && courses.findByCourseId(courseId) == null) {
            throw ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId));
        }
        List<SectionDomain> active = sections.findAll(normalizedSemester, year).stream()
                .filter(s -> !Boolean.FALSE.equals(s.getActive()))
                .filter(s -> courseId == null || courseId.equalsIgnoreCase(s.getCourseId()))
                .toList();
        if (active.isEmpty()) {
            return List.of();
        }

        // Load each lookup table once instead of per section.
        Map<String, CourseDomain> courseById = courses.findAll().stream()
                .collect(Collectors.toMap(CourseDomain::getCourseId, Function.identity()));
        Map<String, InstructorDomain> instructorByCode = instructors.findAll().stream()
                .collect(Collectors.toMap(InstructorDomain::getInstructorCode, Function.identity()));
        Map<String, List<ClassSlotView>> slotsBySection = schedules
                .findBySectionIds(active.stream().map(SectionDomain::getSectionId).toList()).stream()
                .collect(Collectors.groupingBy(ClassScheduleDomain::getSectionId,
                        Collectors.mapping(cs -> new ClassSlotView(cs.getDayOfWeek(), cs.getStartTime(),
                                cs.getEndTime(), cs.getRoomId()), Collectors.toList())));

        LocalDate today = LocalDate.now();
        List<CatalogSectionView> rows = active.stream()
                .map(s -> {
                    CourseDomain course = courseById.get(s.getCourseId());
                    InstructorDomain instructor = instructorByCode.get(s.getInstructorCode());
                    int capacity = valueOr0(s.getCapacity());
                    int enrolled = valueOr0(s.getEnrollmentCount());
                    int seatsLeft = Math.max(0, capacity - enrolled);
                    boolean beforeDeadline = s.getRegistrationDeadline() == null || !today.isAfter(s.getRegistrationDeadline());
                    return new CatalogSectionView(
                            s.getSectionId(),
                            s.getCourseId(),
                            course != null ? course.getTitle() : null,
                            course != null ? valueOr0(course.getCredits()) : 0,
                            course != null ? course.getDeptId() : null,
                            s.getInstructorCode(),
                            instructor != null ? instructor.getName() : null,
                            s.getSemester(),
                            valueOr0(s.getYear()),
                            capacity,
                            enrolled,
                            seatsLeft,
                            s.getRegistrationDeadline(),
                            beforeDeadline && seatsLeft > 0,
                            slotsBySection.getOrDefault(s.getSectionId(), List.of()));
                })
                .sorted(Comparator.comparing(CatalogSectionView::courseId).thenComparing(CatalogSectionView::sectionId))
                .toList();
        LOGGER.debug("Catalog returned {} sections (semester={}, year={}, course={})",
                rows.size(), normalizedSemester, year, courseId);
        return rows;
    }

    private static int valueOr0(Integer value) {
        return value == null ? 0 : value;
    }
}
