package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.Domain.CourseDomain;
import com.iiitd.erp.Domain.CreateUserDomain;
import com.iiitd.erp.Domain.DepartmentDomain;
import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.ProgramDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.CreateUserResponse;
import com.iiitd.erp.io.dto.response.DepartmentResponse;
import com.iiitd.erp.io.dto.response.InstructorResponse;
import com.iiitd.erp.io.dto.response.LoginResponse;
import com.iiitd.erp.io.dto.response.ProgramResponse;
import com.iiitd.erp.io.dto.response.StudentResponse;
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
}
