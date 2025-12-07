package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.ClassSchedule;
import com.iiitd.erp.persistent.entity.erp_db.Weekday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID> {

    @Query(value = """
            select * from erp_db.class_schedules cs
            where cs.room_id = :roomId
              and cs.day_of_week = :day
              and cs.start_time < :end
              and cs.end_time > :start
              and (:excludeSectionId is null or cs.section_id <> :excludeSectionId)
            """, nativeQuery = true)
    List<ClassSchedule> roomConflicts(@Param("roomId") String roomId,
                                      @Param("day") String day,
                                      @Param("start") LocalTime start,
                                      @Param("end") LocalTime end,
                                      @Param("excludeSectionId") String excludeSectionId);

    @Query(value = """
            select cs.* from erp_db.class_schedules cs
            join erp_db.sections s on s.section_id = cs.section_id
            where s.instructor_code = :instructorCode
              and cs.day_of_week = :day
              and cs.start_time < :end
              and cs.end_time > :start
              and (:excludeSectionId is null or cs.section_id <> :excludeSectionId)
            """, nativeQuery = true)
    List<ClassSchedule> instructorConflicts(@Param("instructorCode") String instructorCode,
                                            @Param("day") String day,
                                            @Param("start") LocalTime start,
                                            @Param("end") LocalTime end,
                                            @Param("excludeSectionId") String excludeSectionId);

    List<ClassSchedule> findBySectionIdOrderByDayOfWeekAscStartTimeAsc(String sectionId);

    @Query("""
           select cs from ClassSchedule cs
           join fetch Section s on s.sectionId = cs.sectionId
           where s.instructorCode = :instructorCode
             and s.semester = :semester
             and s.year = :year
           """)
    List<ClassSchedule> findByInstructorAndTerm(@Param("instructorCode") String instructorCode,
                                                @Param("semester") String semester,
                                                @Param("year") Integer year);

    void deleteBySectionId(String sectionId);


    List<ClassSchedule> findBySectionIdInOrderByDayOfWeekAscStartTimeAsc(List<String> sectionIds);
}
