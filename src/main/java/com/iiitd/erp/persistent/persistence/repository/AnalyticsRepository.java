package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.Domain.AnalyticsView.DailyCount;
import com.iiitd.erp.Domain.AnalyticsView.DepartmentCount;
import com.iiitd.erp.Domain.AnalyticsView.SectionFill;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only aggregate queries for the admin analytics page. */
@Repository
public class AnalyticsRepository {

    private final JdbcTemplate jdbc;

    public AnalyticsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SectionFill> sectionFill(String semester, Integer year) {
        List<Object> args = new ArrayList<>();
        String sql = "select s.section_id, s.course_id, s.enrollment_count, s.capacity from erp_db.sections s where s.is_active"
                + termFilter(semester, year, args)
                + " order by s.enrollment_count::float / s.capacity desc, s.section_id";
        return jdbc.query(sql, (rs, i) -> new SectionFill(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)), args.toArray());
    }

    public List<DepartmentCount> enrollmentsByDepartment(String semester, Integer year) {
        List<Object> args = new ArrayList<>();
        String sql = """
                select c.dept_id, count(*) from erp_db.enrollments e
                join erp_db.sections s on s.section_id = e.section_id
                join erp_db.course_details c on c.course_id = s.course_id
                where e.status = 'enrolled'""" + termFilter(semester, year, args) + " group by c.dept_id order by count(*) desc";
        return jdbc.query(sql, (rs, i) -> new DepartmentCount(rs.getString(1), rs.getLong(2)), args.toArray());
    }

    public Map<String, Long> gradeCounts(String semester, Integer year) {
        List<Object> args = new ArrayList<>();
        String sql = """
                select g.final_grade, count(*) from erp_db.grades g
                join erp_db.enrollments e on e.enrollment_id = g.enrollment_id
                join erp_db.sections s on s.section_id = e.section_id
                where g.component = 'FINAL' and g.final_grade is not null""" + termFilter(semester, year, args) + " group by g.final_grade";
        Map<String, Long> counts = new LinkedHashMap<>();
        jdbc.query(sql, rs -> { counts.put(rs.getString(1), rs.getLong(2)); }, args.toArray());
        return counts;
    }

    public List<DailyCount> registrationsByDay(int days) {
        return jdbc.query("""
                select cast(e.enrollment_date as date) as day, count(*) from erp_db.enrollments e
                where e.enrollment_date >= current_date - ? * interval '1 day'
                group by day order by day
                """, (rs, i) -> new DailyCount(rs.getDate(1).toLocalDate(), rs.getLong(2)), days);
    }

    private static String termFilter(String semester, Integer year, List<Object> args) {
        StringBuilder sb = new StringBuilder();
        if (semester != null) {
            sb.append(" and s.semester = ?");
            args.add(semester);
        }
        if (year != null) {
            sb.append(" and s.year = ?");
            args.add(year);
        }
        return sb.toString();
    }
}
