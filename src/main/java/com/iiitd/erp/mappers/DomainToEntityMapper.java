package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import com.iiitd.erp.persistent.entity.erp_db.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;

@Mapper
public interface DomainToEntityMapper {
    DomainToEntityMapper DOMAIN_TO_ENTITY_MAPPER_INSTANCE = Mappers.getMapper(DomainToEntityMapper.class);

    UserAuth domainToUserAuthDomain(UserAuthDomain userAuthDomain);

    @Mapping(target = "studentName", source = "name")
    @Mapping(target = "rollNo", expression = "java(toString(studentUserDomain.getRollNo()))")
    @Mapping(target = "programId", source = "programId")
    @Mapping(target = "year", expression = "java(parseYear(studentUserDomain.getYearOfRegistration()))")
    Student studentUserDomainToStudent(StudentUserDomain studentUserDomain);

    @Mapping(target = "departmentId", source = "deptId")
    Instructor instructorDomainToInstructor(InstructorDomain instructorDomain);

    @Mapping(target = "program_type", source = "programType")
    Program programDomainToProgram(ProgramDomain programDomain);

    Department departmentDomainToDepartment(DepartmentDomain departmentDomain);

    Course courseDomainToCourse(CourseDomain courseDomain);

    Room roomDomainToRoom(RoomDomain roomDomain);

    @Mapping(target = "isActive", source = "active")
    Section sectionDomainToSection(SectionDomain sectionDomain);

    @Mapping(target = "dayOfWeek", expression = "java(toWeekday(classScheduleDomain.getDayOfWeek()))")
    @Mapping(target = "section", ignore = true)
    ClassSchedule classScheduleDomainToClassSchedule(ClassScheduleDomain classScheduleDomain);

    AssessmentComponent assessmentComponentDomainToAssessmentComponent(AssessmentComponentDomain domain);

    AssessmentScore assessmentScoreDomainToAssessmentScore(AssessmentScoreDomain domain);

    Enrollment enrollmentDomainToEnrollment(EnrollmentDomain domain);

    // helpers
    default Weekday toWeekday(DayOfWeek day) { return day == null ? null : Weekday.from(day); }
    default Integer parseYear(String year) { return year == null || year.isBlank() ? null : Integer.valueOf(year); }
    default String toString(Object value) { return value == null ? null : value.toString(); }
}
