package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import com.iiitd.erp.persistent.entity.erp_db.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;

@Mapper
public interface EntityToDomainMapper {
    EntityToDomainMapper ENTITY_TO_DOMAIN_MAPPER_INSTANCE = Mappers.getMapper(EntityToDomainMapper.class);

    UserAuthDomain userAuthToDomain(UserAuth userAuth);

    StudentUserDomain studentToStudentUserDomain(Student student);

    InstructorDomain instructorToInstructorDomain(Instructor instructor);

    ProgramDomain programToProgramDomain(Program program);

    DepartmentDomain departmentToDepartmentDomain(Department department);

    CourseDomain courseToCourseDomain(Course course);

    RoomDomain roomToRoomDomain (Room room);

    SectionDomain sectionToSectionDomain(Section section);

    @Mapping(target = "dayOfWeek", expression = "java(fromWeekday(classSchedule.getDayOfWeek()))")
    @Mapping(target = "roomNumber", ignore = true)
    @Mapping(target = "roomCapacity", ignore = true)
    ClassScheduleDomain classScheduleToClassScheduleDomain(ClassSchedule classSchedule);

    default DayOfWeek fromWeekday(Weekday day) {
        return day == null ? null : day.toJava();
    }

}
