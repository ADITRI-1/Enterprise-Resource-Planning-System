package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.persistent.entity.auth_db.UsersAuth;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EntityToDomainMapper {
    EntityToDomainMapper ENTITY_TO_DOMAIN_MAPPER_INSTANCE = Mappers.getMapper(EntityToDomainMapper.class);

    UserAuthDomain userAuthToDomain (UsersAuth usersAuth);
}
