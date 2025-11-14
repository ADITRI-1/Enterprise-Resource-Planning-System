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
public interface EntityToDomainMapper {
    EntityToDomainMapper ENTITY_TO_DOMAIN_MAPPER_INSTANCE = Mappers.getMapper(EntityToDomainMapper.class);

    UserAuthDomain userAuthToDomain (UserAuth userAuth);

    StudentUserDomain studentToStudentUserDomain (Student student);

    InstructorDomain instructorToInstructorDomain (Instructor instructor);
}
