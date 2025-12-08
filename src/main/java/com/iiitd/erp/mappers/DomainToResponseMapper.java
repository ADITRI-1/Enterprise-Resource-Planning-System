package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.io.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DomainToResponseMapper {
    DomainToResponseMapper DOMAIN_TO_RESPONSE_MAPPER_INSTANCE = Mappers.getMapper(DomainToResponseMapper.class);

    LoginResponse domainToLoginResponse(AuthResultDomain authResultDomain);

    CreateUserResponse domainToCreateUserResponse(CreateUserDomain createUserDomain);

    StudentResponse studentDomainToResponse(StudentUserDomain studentUserDomain);

    InstructorResponse instructorDomainToResponse(InstructorDomain instructorDomain);

    ProgramResponse programDomainToResponse(ProgramDomain programDomain);

    DepartmentResponse departmentDomainToResponse(DepartmentDomain departmentDomain);

    CourseResponse courseDomainToResponse(CourseDomain courseDomain);

    RoomResponse roomDomainToResponse(RoomDomain roomDomain);

    @Mapping(target = "year", expression = "java(sectionDomain.getYear() == null ? 0 : sectionDomain.getYear())")
    @Mapping(target = "active", expression = "java(Boolean.TRUE.equals(sectionDomain.getActive()))")
    SectionResponse sectionDomainToResponse(SectionDomain sectionDomain);

    ScheduleSlotResponse classScheduleDomainToScheduleSlotResponse(ClassScheduleDomain classScheduleDomain);

    AssessmentComponentResponse assessmentComponentDomainToResponse(AssessmentComponentDomain domain);

    InstructorSectionResponse sectionDomainToInstructorSectionResponse(SectionDomain sectionDomain);

    @Mapping(target = "year", expression = "java(domain.getYear() == null ? 0 : domain.getYear())")
    InstructorTimetableSlotResponse instructorTimetableSlotDomainToResponse(InstructorTimetableSlotDomain domain);

    RegistrationResult registrationOutcomeToResponse(RegistrationOutcomeDomain domain);

    TimetableSlot studentTimeTableSlotToResponse(StudentTimeTableSlotDomain domain);

    TimetableSearchSlotResponse timetableSearchSlotDomainToResponse(TimetableSearchSlotDomain domain);

    GradeRow studentGradeDomainToResponse(StudentGradeDomain domain);
}
