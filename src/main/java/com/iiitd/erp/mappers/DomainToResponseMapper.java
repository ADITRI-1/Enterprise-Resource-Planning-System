package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.AuthResultDomain;
import com.iiitd.erp.io.dto.response.LoginResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DomainToResponseMapper {
    DomainToResponseMapper DOMAIN_TO_RESPONSE_MAPPER_INSTANCE = Mappers.getMapper(DomainToResponseMapper.class);

    LoginResponse domainToLoginResponse(AuthResultDomain authResultDomain);
}
