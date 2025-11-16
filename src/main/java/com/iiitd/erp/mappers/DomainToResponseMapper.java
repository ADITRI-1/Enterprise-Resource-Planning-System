package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.io.dto.response.*;
import org.mapstruct.Mapper;
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

    SectionResponse sectionDomainToResponse(SectionDomain sectionDomain);

    ScheduleSlotResponse classScheduleDomainToScheduleSlotResponse(ClassScheduleDomain classScheduleDomain);

}
