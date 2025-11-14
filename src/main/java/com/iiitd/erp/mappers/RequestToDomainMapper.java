package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.LoginRequestDomain;
import com.iiitd.erp.Domain.ProgramDomain;
import com.iiitd.erp.Domain.DepartmentDomain;
import com.iiitd.erp.Domain.CourseDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.io.dto.request.CourseCreateRequest;
import com.iiitd.erp.io.dto.request.CourseUpdateRequest;
import com.iiitd.erp.io.dto.request.DepartmentCreateRequest;
import com.iiitd.erp.io.dto.request.DepartmentUpdateRequest;
import com.iiitd.erp.io.dto.request.InstructorCreateRequest;
import com.iiitd.erp.io.dto.request.InstructorUpdateRequest;
import com.iiitd.erp.io.dto.request.LoginRequest;
import com.iiitd.erp.io.dto.request.ProgramCreateRequest;
import com.iiitd.erp.io.dto.request.ProgramUpdateRequest;
import com.iiitd.erp.io.dto.request.StudentCreateRequest;
import com.iiitd.erp.io.dto.request.StudentUpdateRequest;
import org.mapstruct.Mapper;
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
}
