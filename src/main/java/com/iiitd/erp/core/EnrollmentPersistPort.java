package com.iiitd.erp.core;

import com.iiitd.erp.Domain.EnrollmentDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentPersistPort {
    EnrollmentDomain save(EnrollmentDomain domain);
    Optional<EnrollmentDomain> findByStudentAndSection(UUID studentId, String sectionId);
    long countEnrolled(String sectionId);
    List<EnrollmentDomain> findActiveBySection(String sectionId);
    List<EnrollmentDomain> findByStudentAndStatus(UUID studentId, String status);
    List<EnrollmentDomain> findByStudentAndStatuses(UUID studentId, List<String> statuses);
}
