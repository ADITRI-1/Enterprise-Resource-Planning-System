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

    Student studentUserDomainToStudent(StudentUserDomain studentUserDomain);

    Instructor instructorDomainToInstructor(InstructorDomain instructorDomain);

    Program programDomainToProgram(ProgramDomain programDomain);

    Department departmentDomainToDepartment(DepartmentDomain departmentDomain);

    Course courseDomainToCourse(CourseDomain courseDomain);

    Room roomDomainToRoom(RoomDomain roomDomain);

    Section sectionDomainToSection(SectionDomain sectionDomain);

    @Mapping(target = "dayOfWeek", expression = "java(toWeekday(classScheduleDomain.getDayOfWeek()))")
    @Mapping(target = "section", ignore = true)
    ClassSchedule classScheduleDomainToClassSchedule(ClassScheduleDomain classScheduleDomain);

    default Weekday toWeekday(DayOfWeek day) {
        return day == null ? null : Weekday.from(day);
    }

    AssessmentComponent assessmentComponentDomainToAssessmentComponent(AssessmentComponentDomain domain);

    AssessmentScore assessmentScoreDomainToAssessmentScore(AssessmentScoreDomain domain);

}
