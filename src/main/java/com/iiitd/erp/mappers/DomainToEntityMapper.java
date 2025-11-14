package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DomainToEntityMapper {
    DomainToEntityMapper DOMAIN_TO_ENTITY_MAPPER_INSTANCE = Mappers.getMapper(DomainToEntityMapper.class);

    UserAuth domainToUserAuthDomain  (UserAuthDomain userAuthDomain);
}
