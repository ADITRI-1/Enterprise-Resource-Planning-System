package com.iiitd.erp.core.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PrerequisiteService {
    List<String> prerequisitesOf(String courseId);

    /** courseId -> its prerequisite course ids. */
    Map<String, List<String>> all();

    /** Replaces a course's prerequisites; rejects unknown courses, self-references and cycles. */
    List<String> replace(String courseId, List<String> prerequisiteIds);

    /** Prerequisites of the course the student hasn't passed yet (empty = eligible). */
    List<String> missingFor(UUID studentId, String courseId);
}
