package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.EnrollmentDomain;
import com.iiitd.erp.core.EnrollmentPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Enrollment;
import com.iiitd.erp.persistent.persistence.repository.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class EnrollmentPersistPortImpl implements EnrollmentPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrollmentPersistPortImpl.class);
    private final EnrollmentRepository repo;

    public EnrollmentPersistPortImpl(EnrollmentRepository repo) {
        this.repo = repo;
    }

    @Override
    public EnrollmentDomain save(EnrollmentDomain domain) {
        try {
            Enrollment saved = repo.save(DOMAIN_TO_ENTITY_MAPPER_INSTANCE.enrollmentDomainToEnrollment(domain));
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.enrollmentToEnrollmentDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to save enrollment {}", domain.getEnrollmentId(), ex);
            throw ErpException.wrap(ErrorCode.ENROLLMENT_NOT_FOUND, ex, Map.of("enrollmentId", domain.getEnrollmentId()));
        }
    }

    @Override
    public Optional<EnrollmentDomain> findByStudentAndSection(UUID studentId, String sectionId) {
        return repo.findByStudentIdAndSectionId(studentId, sectionId)
                .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::enrollmentToEnrollmentDomain);
    }

    @Override
    public long countEnrolled(String sectionId) {
        return repo.countBySectionIdAndStatusIgnoreCase(sectionId, "ENROLLED");
    }

    @Override
    public List<EnrollmentDomain> findActiveBySection(String sectionId) {
        return repo.findActiveStudents(sectionId).stream()
                .map(p -> EnrollmentDomain.builder()
                        .enrollmentId(p.getEnrollmentId())
                        .studentId(p.getStudentId())
                        .sectionId(sectionId)
                        .status("ENROLLED")
                        .build())
                .toList();
    }

    @Override
    public List<EnrollmentDomain> findByStudentAndStatus(UUID studentId, String status) {
        return repo.findByStudentIdAndStatusIgnoreCase(studentId, status).stream()
                .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::enrollmentToEnrollmentDomain)
                .toList();
    }

    @Override
    public List<EnrollmentDomain> findByStudentAndStatuses(UUID studentId, List<String> statuses) {
        return repo.findByStudentIdAndStatusIn(studentId, statuses).stream()
                .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::enrollmentToEnrollmentDomain)
                .toList();
    }
}
