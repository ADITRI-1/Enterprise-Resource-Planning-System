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

    @Query("""
           select cs from ClassSchedule cs
           where cs.roomId = :roomId
             and cs.dayOfWeek = :day
             and cs.startTime < :end
             and cs.endTime > :start
             and (:excludeSectionId is null or cs.sectionId <> :excludeSectionId)
           """)
    List<ClassSchedule> roomConflicts(@Param("roomId") String roomId,
                                      @Param("day") Weekday day,
                                      @Param("start") LocalTime start,
                                      @Param("end") LocalTime end,
                                      @Param("excludeSectionId") String excludeSectionId);

    @Query("""
           select cs from ClassSchedule cs
           join Section s on s.sectionId = cs.sectionId
           where s.instructorCode = :instructorCode
             and cs.dayOfWeek = :day
             and cs.startTime < :end
             and cs.endTime > :start
             and (:excludeSectionId is null or cs.sectionId <> :excludeSectionId)
           """)
    List<ClassSchedule> instructorConflicts(@Param("instructorCode") String instructorCode,
                                            @Param("day") Weekday day,
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
}
