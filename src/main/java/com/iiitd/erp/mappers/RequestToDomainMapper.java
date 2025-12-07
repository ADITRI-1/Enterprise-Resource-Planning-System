package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.io.dto.request.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RequestToDomainMapper {
    RequestToDomainMapper REQUEST_TO_DOMAIN_MAPPER_INSTANCE = Mappers.getMapper(RequestToDomainMapper.class);

    LoginRequestDomain loginRequestToDomain(LoginRequest loginRequest);

    StudentUserDomain studentUserRequestToDomain(StudentCreateRequest studentCreateRequest);

    StudentUserDomain studentUpdateRequestToDomain(StudentUpdateRequest studentUpdateRequest);

    InstructorDomain instructorCreateRequestToDomain(InstructorCreateRequest instructorCreateRequest);

    InstructorDomain instructorUpdateRequestToDomain(InstructorUpdateRequest instructorUpdateRequest);

    ProgramDomain programCreateRequestToDomain(ProgramCreateRequest programCreateRequest);

    ProgramDomain programUpdateRequestToDomain(ProgramUpdateRequest programUpdateRequest);

    DepartmentDomain departmentCreateRequestToDomain(DepartmentCreateRequest departmentCreateRequest);

    DepartmentDomain departmentUpdateRequestToDomain(DepartmentUpdateRequest departmentUpdateRequest);

    CourseDomain courseCreateRequestToDomain(CourseCreateRequest courseCreateRequest);

    CourseDomain courseUpdateRequestToDomain(CourseUpdateRequest courseUpdateRequest);

    RoomDomain roomCreateRequestToDomain(RoomCreateRequest request);

    RoomDomain roomUpdateRequestToDomain(RoomUpdateRequest request);

    SectionDomain sectionCreateRequestToDomain(SectionCreateRequest request);

    SectionDomain sectionUpdateRequestToDomain(SectionUpdateRequest request);

    @Mapping(source= "day", target = "dayOfWeek")
    ClassScheduleDomain scheduleSlotRequestToDomain(ScheduleSlotRequest request);

    AssessmentComponentDomain assessmentComponentRequestToDomain(AssessmentComponentRequest request);
}
