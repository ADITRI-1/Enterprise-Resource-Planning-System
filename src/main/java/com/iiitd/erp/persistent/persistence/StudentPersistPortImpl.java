package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.StudentUserDomain;
import com.iiitd.erp.core.StudentPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Student;
import com.iiitd.erp.persistent.persistence.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class StudentPersistPortImpl implements StudentPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentPersistPortImpl.class);

    private final StudentRepository studentRepository;

    public StudentPersistPortImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public StudentUserDomain findByRollNo(Integer rollNo) {
        LOGGER.debug("Fetching student entity for roll {}", rollNo);
        try {
            Student student = studentRepository.findByRollNo(rollNo);
            if (student == null) return null;
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.studentToStudentUserDomain(student);
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch student for roll {}", rollNo, ex);
            throw ErpException.wrap(
                    ErrorCode.STUDENT_FETCH_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    @Override
    public boolean saveStudent(StudentUserDomain studentDomain) {
        Integer rollNo = studentDomain.getRollNo();
        LOGGER.debug("Persisting student entity for roll {}", rollNo);
        try {
            Student student = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.studentUserDomainToStudent(studentDomain);
            studentRepository.save(student);
            LOGGER.info("Persisted student entity for roll {}", rollNo);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to persist student {}", rollNo, ex);
            throw ErpException.wrap(
                    ErrorCode.STUDENT_PERSIST_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    @Override
    public StudentUserDomain updateStudent(StudentUserDomain studentDomain) {
        Integer rollNo = studentDomain.getRollNo();
        LOGGER.debug("Updating student entity for roll {}", rollNo);
        Student existing = studentRepository.findByRollNo(rollNo);
        if (existing == null) {
            throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("rollNo", rollNo));
        }
        try {
            Student student = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.studentUserDomainToStudent(studentDomain);
            student.setStudentId(existing.getStudentId());
            Student updated = studentRepository.save(student);
            LOGGER.info("Updated student entity for roll {}", rollNo);
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.studentToStudentUserDomain(updated);
        } catch (Exception ex) {
            LOGGER.error("Failed to update student {}", rollNo, ex);
            throw ErpException.wrap(
                    ErrorCode.STUDENT_UPDATE_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }

    @Override
    public boolean deleteStudent(Integer rollNo) {
        LOGGER.debug("Deleting student entity for roll {}", rollNo);
        Student existing = studentRepository.findByRollNo(rollNo);
        if (existing == null) {
            throw ErpException.of(ErrorCode.STUDENT_NOT_FOUND, Map.of("rollNo", rollNo));
        }
        try {
            studentRepository.delete(existing);
            LOGGER.info("Deleted student entity for roll {}", rollNo);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to delete student {}", rollNo, ex);
            throw ErpException.wrap(
                    ErrorCode.STUDENT_DELETE_FAILED, ex, Map.of("rollNo", rollNo));
        }
    }
}
