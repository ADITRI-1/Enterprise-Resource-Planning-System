package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.CourseDomain;
import com.iiitd.erp.core.CoursePersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Course;
import com.iiitd.erp.persistent.persistence.repository.CourseAdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class CoursePersistPortImpl implements CoursePersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoursePersistPortImpl.class);

    private final CourseAdminRepository courseRepository;

    public CoursePersistPortImpl(CourseAdminRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public CourseDomain findByCourseId(String courseId) {
        LOGGER.debug("Fetching course {}", courseId);
        try {
            return courseRepository.findByCourseId(courseId)
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::courseToCourseDomain)
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch course {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.COURSE_FETCH_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    @Override
    public CourseDomain save(CourseDomain courseDomain) {
        LOGGER.debug("Persisting course {}", courseDomain.getCourseId());
        try {
            Course entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.courseDomainToCourse(courseDomain);
            Course saved = courseRepository.save(entity);
            LOGGER.info("Persisted course {}", courseDomain.getCourseId());
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.courseToCourseDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist course {}", courseDomain.getCourseId(), ex);
            throw ErpException.wrap(ErrorCode.COURSE_CREATE_FAILED, ex, Map.of("courseId", courseDomain.getCourseId()));
        }
    }

    @Override
    public CourseDomain update(CourseDomain courseDomain) {
        String courseId = courseDomain.getCourseId();
        courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId)));
        try {
            Course entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.courseDomainToCourse(courseDomain);
            Course saved = courseRepository.save(entity);
            LOGGER.info("Updated course {}", courseId);
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.courseToCourseDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to update course {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.COURSE_UPDATE_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    @Override
    public boolean deleteByCourseId(String courseId) {
        Course existing = courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId)));
        try {
            courseRepository.delete(existing);
            LOGGER.info("Deleted course {}", courseId);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to delete course {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.COURSE_DELETE_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    @Override
    public List<CourseDomain> findAll() {
        LOGGER.debug("Fetching all courses");
        try {
            return courseRepository.findAll().stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::courseToCourseDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch courses", ex);
            throw ErpException.wrap(ErrorCode.COURSE_FETCH_FAILED, ex, Map.of());
        }
    }
}
