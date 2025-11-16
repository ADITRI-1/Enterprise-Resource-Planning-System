package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.ClassScheduleDomain;
import com.iiitd.erp.Domain.RoomDomain;
import com.iiitd.erp.Domain.SectionDomain;
import com.iiitd.erp.core.ClassSchedulePersistPort;
import com.iiitd.erp.core.RoomPersistPort;
import com.iiitd.erp.core.SectionPersistPort;
import com.iiitd.erp.core.service.AvailabilityService;
import com.iiitd.erp.core.service.SectionScheduleService;
import com.iiitd.erp.core.service.dto.SectionScheduleResult;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SectionScheduleServiceImpl implements SectionScheduleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SectionScheduleServiceImpl.class);

    private final SectionPersistPort sections;
    private final ClassSchedulePersistPort schedules;
    private final RoomPersistPort rooms;
    private final AvailabilityService availability;

    public SectionScheduleServiceImpl(SectionPersistPort sections,
                                      ClassSchedulePersistPort schedules,
                                      RoomPersistPort rooms,
                                      AvailabilityService availability) {
        this.sections = sections;
        this.schedules = schedules;
        this.rooms = rooms;
        this.availability = availability;
    }

    @Override
    public SectionScheduleResult view(String sectionId) {
        SectionDomain section = requireSection(sectionId);
        List<ClassScheduleDomain> slots = enrich(schedules.findBySectionId(sectionId));
        LOGGER.info("Fetched {} schedule slots for section {}", slots.size(), sectionId);
        return new SectionScheduleResult(section, slots);
    }

    @Override
    public SectionScheduleResult replace(String sectionId, List<ClassScheduleDomain> requestedSlots) {
        SectionDomain section = requireSection(sectionId);
        List<ClassScheduleDomain> normalized = validate(section, requestedSlots);
        List<ClassScheduleDomain> saved = schedules.replaceSectionSchedule(sectionId, normalized);
        LOGGER.info("Saved {} schedule slots for section {}", saved.size(), sectionId);
        return new SectionScheduleResult(section, enrich(saved));
    }

    @Override
    public void clear(String sectionId) {
        requireSection(sectionId);
        schedules.deleteBySectionId(sectionId);
        LOGGER.info("Cleared schedule for section {}", sectionId);
    }

    private SectionDomain requireSection(String sectionId) {
        SectionDomain section = sections.findBySectionId(sectionId);
        if (section == null) {
            throw ErpException.of(ErrorCode.SECTION_NOT_FOUND, Map.of("sectionId", sectionId));
        }
        return section;
    }

    private List<ClassScheduleDomain> validate(SectionDomain section, List<ClassScheduleDomain> slots) {
        if (slots == null || slots.isEmpty()) {
            throw ErpException.of(ErrorCode.SCHEDULE_SLOT_INVALID, Map.of("sectionId", section.getSectionId()));
        }
        Map<String, RoomDomain> roomCache = new HashMap<>();
        List<ClassScheduleDomain> normalized = new ArrayList<>(slots.size());
        for (ClassScheduleDomain slot : slots) {
            DayOfWeek day = Objects.requireNonNull(slot.getDayOfWeek(), "day");
            LocalTime start = Objects.requireNonNull(slot.getStartTime(), "startTime");
            LocalTime end = Objects.requireNonNull(slot.getEndTime(), "endTime");
            if (!start.isBefore(end)) {
                throw ErpException.of(ErrorCode.SCHEDULE_SLOT_INVALID,
                        Map.of("sectionId", section.getSectionId(), "day", day, "start", start, "end", end));
            }
            RoomDomain room = roomCache.computeIfAbsent(slot.getRoomId(), id -> {
                RoomDomain found = rooms.findByRoomId(id);
                if (found == null) {
                    throw ErpException.of(ErrorCode.ROOM_NOT_FOUND, Map.of("roomId", id));
                }
                return found;
            });
            if (room.getCapacity() < section.getCapacity()) {
                throw ErpException.of(ErrorCode.SCHEDULE_ROOM_CAPACITY_MISMATCH,
                        Map.of("roomId", room.getRoomId(),
                                "roomCapacity", room.getCapacity(),
                                "sectionId", section.getSectionId(),
                                "requiredCapacity", section.getCapacity()));
            }
            availability.ensureSlotFree(section.getInstructorCode(),
                    section.getSectionId(),
                    room.getRoomId(),
                    day,
                    start,
                    end);
            normalized.add(slot
                    .withSectionId(section.getSectionId())
                    .withScheduleId(slot.getScheduleId())
                    .withRoomNumber(room.getRoomNumber())
                    .withRoomCapacity(room.getCapacity()));
        }
        ensureNoInternalClashes(normalized);
        return normalized;
    }

    private void ensureNoInternalClashes(List<ClassScheduleDomain> slots) {
        Map<DayOfWeek, List<ClassScheduleDomain>> byDay = slots.stream()
                .collect(Collectors.groupingBy(ClassScheduleDomain::getDayOfWeek));
        for (Map.Entry<DayOfWeek, List<ClassScheduleDomain>> entry : byDay.entrySet()) {
            LocalTime lastEnd = null;
            for (ClassScheduleDomain slot : entry.getValue().stream()
                    .sorted(Comparator.comparing(ClassScheduleDomain::getStartTime))
                    .toList()) {
                if (lastEnd != null && slot.getStartTime().isBefore(lastEnd)) {
                    throw ErpException.of(ErrorCode.SCHEDULE_SLOT_INVALID,
                            Map.of("day", entry.getKey(),
                                    "start", slot.getStartTime(),
                                    "end", slot.getEndTime()));
                }
                lastEnd = slot.getEndTime();
            }
        }
    }

    private List<ClassScheduleDomain> enrich(List<ClassScheduleDomain> slots) {
        if (slots.isEmpty()) {
            return slots;
        }
        Map<String, RoomDomain> roomCache = new HashMap<>();
        return slots.stream()
                .map(slot -> {
                    RoomDomain room = roomCache.computeIfAbsent(slot.getRoomId(), rooms::findByRoomId);
                    if (room == null) {
                        return slot;
                    }
                    return slot.withRoomNumber(room.getRoomNumber())
                            .withRoomCapacity(room.getCapacity());
                })
                .collect(Collectors.toList());
    }
}
