package com.iiitd.erp.persistent.persistence.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

/** course_prerequisites is a plain join table, so it is accessed with SQL rather than an entity. */
@Repository
public class PrerequisiteRepository {

    private final JdbcTemplate jdbc;

    public PrerequisiteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<String> prerequisitesOf(String courseId) {
        return jdbc.queryForList(
                "select prerequisite_id from erp_db.course_prerequisites where course_id = ? order by prerequisite_id",
                String.class, courseId);
    }

    public Map<String, List<String>> all() {
        Map<String, List<String>> map = new HashMap<>();
        jdbc.query("select course_id, prerequisite_id from erp_db.course_prerequisites order by prerequisite_id",
                rs -> { map.computeIfAbsent(rs.getString(1), k -> new ArrayList<>()).add(rs.getString(2)); });
        return map;
    }

    public void replace(String courseId, Collection<String> prerequisiteIds) {
        jdbc.update("delete from erp_db.course_prerequisites where course_id = ?", courseId);
        for (String prerequisite : prerequisiteIds) {
            jdbc.update("insert into erp_db.course_prerequisites (course_id, prerequisite_id) values (?, ?)",
                    courseId, prerequisite);
        }
    }

    /** Courses the student has passed: a computed final grade other than F in any section. */
    public Set<String> passedCourses(UUID studentId) {
        return new HashSet<>(jdbc.queryForList("""
                select distinct s.course_id
                from erp_db.grades g
                join erp_db.enrollments e on e.enrollment_id = g.enrollment_id
                join erp_db.sections s on s.section_id = e.section_id
                where e.student_id = ? and g.component = 'FINAL' and g.final_grade is not null and g.final_grade <> 'F'
                """, String.class, studentId));
    }
}
