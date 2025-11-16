package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.core.InstructorPersistPort;
import com.iiitd.erp.core.service.AvailabilityService;
import com.iiitd.erp.core.service.WorkingHoursService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.io.dto.response.InstructorAvailabilityResponse;
import com.iiitd.erp.io.dto.response.RoomAvailabilityResponse;
import com.iiitd.erp.persistent.entity.erp_db.Weekday;
import com.iiitd.erp.persistent.persistence.repository.ClassScheduleRepository;
import com.iiitd.erp.persistent.persistence.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iiitd.erp.io.dto.response.InstructorAvailabilityResponse.BusySlot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvailabilityServiceImpl.class);

    private final RoomRepository rooms;
    private final ClassScheduleRepository schedules;
    private final InstructorPersistPort instructors;
    private final WorkingHoursService workingHours;

    public AvailabilityServiceImpl(RoomRepository rooms,
                                   ClassScheduleRepository schedules,
                                   InstructorPersistPort instructors,
                                   WorkingHoursService workingHours) {
        this.rooms = rooms;
        this.schedules = schedules;
        this.instructors = instructors;
        this.workingHours = workingHours;
    }

    @Override
    public List<RoomAvailabilityResponse> availableRooms(DayOfWeek day,
                                                         LocalTime start,
                                                         LocalTime end,
                                                         int minCapacity) {
        workingHours.ensureWithin(start, end);
        Weekday weekday = Weekday.from(day);
        return rooms.findByCapacityGreaterThanEqual(minCapacity).stream()
                .filter(room -> schedules.roomConflicts(room.getRoomId(), weekday, start, end, null).isEmpty())
                .map(room -> RoomAvailabilityResponse.builder()
                        .roomId(room.getRoomId())
                        .roomName(room.getRoomNumber())
                        .capacity(room.getCapacity())
                        .build())
                .toList();
    }

    @Override
    public InstructorAvailabilityResponse instructorAvailability(String instructorCode,
                                                                 String semester,
                                                                 int year) {
        InstructorDomain instructor = instructors.findByInstructorCode(instructorCode);
        if (instructor == null) {
            throw ErpException.of(ErrorCode.INSTRUCTOR_NOT_FOUND,
                    Map.of("instructorCode", instructorCode));
        }

        Map<DayOfWeek, List<BusySlot>> busy = schedules.findByInstructorAndTerm(instructorCode, semester, year).stream()
                .collect(Collectors.groupingBy(cs -> cs.getDayOfWeek().toJava(),
                        Collectors.mapping(cs -> new BusySlot(
                                        cs.getStartTime(),
                                        cs.getEndTime(),
                                        cs.getSection() != null ? cs.getSection().getCourseId() : null,
                                        cs.getSectionId()),
                                Collectors.toList())));

        return InstructorAvailabilityResponse.builder()
                .instructorCode(instructorCode)
                .busy(busy)
                .build();
    }

    @Override
    public void ensureSlotFree(String instructorCode,
                               String sectionIdToIgnore,
                               String roomId,
                               DayOfWeek day,
                               LocalTime start,
                               LocalTime end) {
        workingHours.ensureWithin(start, end);
        Weekday weekday = Weekday.from(day);

        if (!schedules.roomConflicts(roomId, weekday, start, end, sectionIdToIgnore).isEmpty()) {
            LOGGER.warn("Room conflict for {} {} {}-{}", roomId, day, start, end);
            throw ErpException.of(ErrorCode.SCHEDULE_ROOM_CONFLICT,
                    Map.of("roomId", roomId, "day", day, "start", start, "end", end));
        }

        if (!schedules.instructorConflicts(instructorCode, weekday, start, end, sectionIdToIgnore).isEmpty()) {
            LOGGER.warn("Instructor {} conflict {} {}-{}", instructorCode, day, start, end);
            throw ErpException.of(ErrorCode.SCHEDULE_INSTRUCTOR_CONFLICT,
                    Map.of("instructorCode", instructorCode, "day", day, "start", start, "end", end));
        }
    }

}
