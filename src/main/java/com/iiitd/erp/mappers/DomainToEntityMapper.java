package com.iiitd.erp.mappers;

import com.iiitd.erp.Domain.InstructorDomain;
import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.Domain.UserAuthDomain;
import com.iiitd.erp.persistent.entity.auth_db.UserAuth;
import com.iiitd.erp.persistent.entity.erp_db.Instructor;
import com.iiitd.erp.persistent.entity.erp_db.Student;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DomainToEntityMapper {
    DomainToEntityMapper DOMAIN_TO_ENTITY_MAPPER_INSTANCE = Mappers.getMapper(DomainToEntityMapper.class);

    UserAuth domainToUserAuthDomain  (UserAuthDomain userAuthDomain);

    Student studentUserDomainToStudent (StudentUserDomain studentUserDomain);

    Instructor instructorDomainToInstructor (InstructorDomain instructorDomain);
}
