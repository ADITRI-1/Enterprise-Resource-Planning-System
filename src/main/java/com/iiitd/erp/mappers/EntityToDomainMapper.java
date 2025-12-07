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

    @Mapping(target = "name", source = "studentName")
    @Mapping(target = "yearOfRegistration", expression = "java(toYearString(student.getYear()))")
    StudentUserDomain studentToStudentUserDomain(Student student);

    @Mapping(target = "deptId", source = "departmentId")
    InstructorDomain instructorToInstructorDomain(Instructor instructor);

    @Mapping(target = "programType", source = "programType")
    ProgramDomain programToProgramDomain(Program program);

    DepartmentDomain departmentToDepartmentDomain(Department department);

    CourseDomain courseToCourseDomain(Course course);

    RoomDomain roomToRoomDomain (Room room);

    @Mapping(target = "active", source = "isActive")
    SectionDomain sectionToSectionDomain(Section section);

    @Mapping(target = "dayOfWeek", expression = "java(fromWeekday(classSchedule.getDayOfWeek()))")
    @Mapping(target = "roomNumber", ignore = true)
    @Mapping(target = "roomCapacity", ignore = true)
    ClassScheduleDomain classScheduleToClassScheduleDomain(ClassSchedule classSchedule);

    AssessmentComponentDomain assessmentComponentToDomain(AssessmentComponent entity);

    AssessmentScoreDomain assessmentScoreToDomain(AssessmentScore entity);

    EnrollmentDomain enrollmentToEnrollmentDomain(Enrollment entity);

    // helpers
    default DayOfWeek fromWeekday(Weekday day) { return day == null ? null : day.toJava(); }
    default String toYearString(Integer year) { return year == null ? null : year.toString(); }
}
