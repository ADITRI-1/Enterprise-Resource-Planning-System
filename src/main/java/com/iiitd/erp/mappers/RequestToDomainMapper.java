package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.LoginRequestDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.io.dto.request.LoginRequest;
import com.iiitd.erp.io.dto.request.StudentCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RequestToDomainMapper {
    RequestToDomainMapper REQUEST_TO_DOMAIN_MAPPER_INSTANCE = Mappers.getMapper(RequestToDomainMapper.class);

    LoginRequestDomain loginRequestToDomain (LoginRequest loginRequest);

    StudentUserDomain studentUserRequestToDomain (StudentCreateRequest studentCreateRequest);
}
