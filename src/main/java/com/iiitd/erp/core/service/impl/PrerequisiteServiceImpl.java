package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.core.CoursePersistPort;
import com.iiitd.erp.core.service.PrerequisiteService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.persistence.repository.PrerequisiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class PrerequisiteServiceImpl implements PrerequisiteService {

    private final PrerequisiteRepository prerequisites;
    private final CoursePersistPort courses;

    public PrerequisiteServiceImpl(PrerequisiteRepository prerequisites, CoursePersistPort courses) {
        this.prerequisites = prerequisites;
        this.courses = courses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> prerequisitesOf(String courseId) {
        requireCourse(courseId);
        return prerequisites.prerequisitesOf(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<String>> all() {
        return prerequisites.all();
    }

    @Override
    public List<String> replace(String courseId, List<String> prerequisiteIds) {
        requireCourse(courseId);
        Set<String> wanted = new TreeSet<>(prerequisiteIds == null ? List.of() : prerequisiteIds);
        for (String id : wanted) {
            if (id.equalsIgnoreCase(courseId)) {
                throw invalid("A course can't require itself", id);
            }
            if (courses.findByCourseId(id) == null) {
                throw invalid("Unknown course", id);
            }
        }
        // Adding courseId -> X creates a cycle if X already (transitively) requires courseId.
        Map<String, List<String>> graph = prerequisites.all();
        graph.put(courseId, new ArrayList<>(wanted));
        for (String id : wanted) {
            if (reaches(graph, id, courseId, new HashSet<>())) {
                throw invalid("That would create a circular requirement", id);
            }
        }
        prerequisites.replace(courseId, wanted);
        return new ArrayList<>(wanted);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> missingFor(UUID studentId, String courseId) {
        List<String> required = prerequisites.prerequisitesOf(courseId);
        if (required.isEmpty()) {
            return List.of();
        }
        Set<String> passed = prerequisites.passedCourses(studentId);
        return required.stream().filter(id -> !passed.contains(id)).toList();
    }

    private static boolean reaches(Map<String, List<String>> graph, String from, String target, Set<String> seen) {
        if (from.equalsIgnoreCase(target)) {
            return true;
        }
        if (!seen.add(from)) {
            return false;
        }
        return graph.getOrDefault(from, List.of()).stream().anyMatch(next -> reaches(graph, next, target, seen));
    }

    private void requireCourse(String courseId) {
        if (courses.findByCourseId(courseId) == null) {
            throw ErpException.of(ErrorCode.COURSE_NOT_FOUND, Map.of("courseId", courseId));
        }
    }

    private static ErpException invalid(String reason, String courseId) {
        return ErpException.of(ErrorCode.PREREQUISITE_INVALID, Map.of("reason", reason, "courseId", courseId));
    }
}
