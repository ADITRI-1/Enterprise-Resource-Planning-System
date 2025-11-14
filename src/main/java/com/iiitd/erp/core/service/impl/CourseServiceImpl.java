package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.CourseDomain;
import com.iiitd.erp.core.CoursePersistPort;
import com.iiitd.erp.core.DepartmentPersistPort;
import com.iiitd.erp.core.service.CourseService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CoursePersistPort coursePersistPort;
    private final DepartmentPersistPort departmentPersistPort;

    public CourseServiceImpl(CoursePersistPort coursePersistPort,
                             DepartmentPersistPort departmentPersistPort) {
        this.coursePersistPort = coursePersistPort;
        this.departmentPersistPort = departmentPersistPort;
    }

    @Override
    public CourseDomain createCourse(CourseDomain courseDomain) {
        String courseId = courseDomain.getCourseId();
        LOGGER.info("Creating course {}", courseId);

        if (coursePersistPort.findByCourseId(courseId) != null) {
            throw ErpException.of(ErrorCode.COURSE_DUPLICATE, Map.of("courseId", courseId));
        }
        ensureDepartmentExists(courseDomain.getDeptId());

        try {
            CourseDomain saved = coursePersistPort.save(courseDomain);
            LOGGER.info("Created course {}", courseId);
            return saved;
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating course {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.COURSE_CREATE_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    @Override
    public CourseDomain updateCourse(CourseDomain courseDomain) {
        String courseId = courseDomain.getCourseId();
        LOGGER.info("Updating course {}", courseId);

        if (coursePersistPort.findByCourseId(courseId) == null) {
            throw ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseDomain.getCourseId()));
        }
        ensureDepartmentExists(courseDomain.getDeptId());

        try {
            CourseDomain updated = coursePersistPort.update(courseDomain);
            LOGGER.info("Updated course {}", courseId);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating course {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.COURSE_UPDATE_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    @Override
    public boolean deleteCourse(String courseId) {
        LOGGER.info("Deleting course {}", courseId);

        if (coursePersistPort.findByCourseId(courseId) == null) {
            throw ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId));
        }

        try {
            boolean deleted = coursePersistPort.deleteByCourseId(courseId);
            LOGGER.info("Deleted course {}: {}", courseId, deleted);
            return deleted;
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting course {}", courseId, ex);
            throw ErpException.wrap(ErrorCode.COURSE_DELETE_FAILED, ex, Map.of("courseId", courseId));
        }
    }

    @Override
    public List<CourseDomain> listCourses() {
        LOGGER.info("Listing all courses");
        try {
            return coursePersistPort.findAll();
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing courses: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing courses", ex);
            throw ErpException.wrap(ErrorCode.COURSE_FETCH_FAILED, ex, Map.of());
        }
    }

    private void ensureDepartmentExists(String deptId) {
        if (deptId == null || deptId.isBlank()) {
            throw ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId));
        }
        if (departmentPersistPort.findByDeptId(deptId) == null) {
            throw ErpException.of(ErrorCode.DEPARTMENT_NOT_FOUND, Map.of("deptId", deptId));
        }
    }
}
