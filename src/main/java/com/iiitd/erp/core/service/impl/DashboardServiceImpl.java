package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.DashboardView;
import com.iiitd.erp.Domain.EnrollmentDomain;
import com.iiitd.erp.core.service.DashboardService;
import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.persistent.persistence.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserAuthRepository users;
    private final CourseAdminRepository courses;
    private final DepartmentRepository departments;
    private final ProgramRepository programs;
    private final RoomRepository rooms;
    private final SectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final MaintenanceService maintenance;

    public DashboardServiceImpl(UserAuthRepository users,
                                CourseAdminRepository courses,
                                DepartmentRepository departments,
                                ProgramRepository programs,
                                RoomRepository rooms,
                                SectionRepository sections,
                                EnrollmentRepository enrollments,
                                MaintenanceService maintenance) {
        this.users = users;
        this.courses = courses;
        this.departments = departments;
        this.programs = programs;
        this.rooms = rooms;
        this.sections = sections;
        this.enrollments = enrollments;
        this.maintenance = maintenance;
    }

    @Override
    public DashboardView adminDashboard() {
        return new DashboardView(
                users.countByRole("student"),
                users.countByRole("instructor"),
                users.countByRole("admin"),
                courses.count(),
                departments.count(),
                programs.count(),
                rooms.count(),
                sections.countByIsActiveTrue(),
                sections.countFullActive(),
                enrollments.countByStatusIgnoreCase(EnrollmentDomain.ENROLLED),
                users.countByLockedUntilAfter(OffsetDateTime.now()),
                users.countByMustChangePasswordTrue(),
                maintenance.isReadOnly());
    }
}
