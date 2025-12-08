package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.InstructorTimetableSlotDomain;
import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.Domain.TimetableSearchSlotDomain;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.service.TimetableService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.entity.erp_db.ClassSchedule;
import com.iiitd.erp.persistent.entity.erp_db.Weekday;
import com.iiitd.erp.persistent.persistence.repository.ClassScheduleRepository;
import com.iiitd.erp.persistent.entity.erp_db.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class TimetableServiceImpl implements TimetableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableServiceImpl.class);

    private final InstructorPersistPort instructors;
    private final SectionPersistPort sections;
    private final ClassScheduleRepository schedules;

    public TimetableServiceImpl(InstructorPersistPort instructors,
                                          SectionPersistPort sections,
                                          ClassScheduleRepository schedules) {
        this.instructors = instructors;
        this.sections = sections;
        this.schedules = schedules;
    }

    @Override
    public List<InstructorTimetableSlotDomain> timetable(String instructorCode, String semester, Integer year) {
        ensureInstructorExists(instructorCode);
        String normalizedSemester = normalizeSemester(semester);
        List<SectionDomain> ownedSections = sections.findByInstructor(instructorCode, normalizedSemester, year);
        if (ownedSections.isEmpty()) {
            LOGGER.info("Instructor {} has no sections for semester={} year={}", instructorCode, normalizedSemester, year);
            return List.of();
        }
        Map<String, SectionDomain> sectionMap = ownedSections.stream()
                .collect(Collectors.toMap(SectionDomain::getSectionId, Function.identity()));

        List<String> sectionIds = new ArrayList<>(sectionMap.keySet());
        List<ClassSchedule> rows = schedules.findBySectionIdInOrderByDayOfWeekAscStartTimeAsc(sectionIds);

        LOGGER.info("Found {} timetable slots for instructor {}", rows.size(), instructorCode);
        return rows.stream()
                .map(cs -> toDomain(cs, sectionMap.get(cs.getSectionId())))
                .sorted(Comparator.comparing(InstructorTimetableSlotDomain::getDayOfWeek)
                        .thenComparing(InstructorTimetableSlotDomain::getStartTime))
                .toList();
    }

    @Override
    public List<TimetableSearchSlotDomain> search(String instructorCode,
                                                  String courseId,
                                                  String sectionId,
                                                  String roomId,
                                                  String semester,
                                                  Integer year,
                                                  DayOfWeek day) {
        if (allFiltersEmpty(instructorCode, courseId, sectionId, roomId, semester, year, day)) {
            throw ErpException.of(ErrorCode.REQUEST_VALIDATION_FAILED,
                    Map.of("filters", "At least one filter must be provided"));
        }

        String normalizedSemester = normalizeSemester(semester);
        Weekday weekday = day != null ? Weekday.from(day) : null;
        List<ClassSchedule> rows = schedules.searchTimetable(instructorCode, courseId, sectionId,
                roomId, normalizedSemester, year, weekday);

        return rows.stream()
                .map(cs -> toSearchDomain(cs, cs.getSection()))
                .toList();
    }

    private void ensureInstructorExists(String instructorCode) {
        if (instructors.findByInstructorCode(instructorCode) == null) {
            LOGGER.warn("Instructor {} not found when building timetable", instructorCode);
            throw ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND, Map.of("instructorCode", instructorCode));
        }
    }

    private String normalizeSemester(String semester) {
        return (semester == null || semester.isBlank()) ? null : semester.trim().toUpperCase(Locale.ROOT);
    }

    private InstructorTimetableSlotDomain toDomain(ClassSchedule cs, SectionDomain section) {
        if (section == null) {
            throw ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", cs.getSectionId()));
        }
        return InstructorTimetableSlotDomain.builder()
                .sectionId(section.getSectionId())
                .courseId(section.getCourseId())
                .semester(section.getSemester())
                .year(section.getYear())
                .dayOfWeek(cs.getDayOfWeek().toJava())
                .startTime(cs.getStartTime())
                .endTime(cs.getEndTime())
                .roomId(cs.getRoomId())
                .build();
    }

    private TimetableSearchSlotDomain toSearchDomain(ClassSchedule cs, Section section) {
        if (section == null) {
            throw ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", cs.getSectionId()));
        }
        return TimetableSearchSlotDomain.builder()
                .sectionId(section.getSectionId())
                .courseId(section.getCourseId())
                .instructorCode(section.getInstructorCode())
                .semester(section.getSemester())
                .year(section.getYear())
                .day(cs.getDayOfWeek().toJava())
                .startTime(cs.getStartTime())
                .endTime(cs.getEndTime())
                .roomId(cs.getRoomId())
                .build();
    }

    private boolean allFiltersEmpty(String instructorCode,
                                    String courseId,
                                    String sectionId,
                                    String roomId,
                                    String semester,
                                    Integer year,
                                    DayOfWeek day) {
        return (instructorCode == null || instructorCode.isBlank())
                && (courseId == null || courseId.isBlank())
                && (sectionId == null || sectionId.isBlank())
                && (roomId == null || roomId.isBlank())
                && (semester == null || semester.isBlank())
                && year == null
                && day == null;
    }
}
