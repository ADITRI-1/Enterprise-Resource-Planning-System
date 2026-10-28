package com.iiitd.erp.integration;

import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Paging, catalog, registrations, class stats, dashboard and API docs. */
class ReadEndpointsIntegrationTest extends PostgresIntegrationTest {

    private static final String STU1 = "33333333-3333-3333-3333-000000000001";

    @Test
    void listEndpointsSupportSearchSortAndPaging() throws Exception {
        MockHttpSession admin = login("admin1", "Admin@123");

        mvc.perform(get("/api/admin/courses").session(admin).param("sort", "title,desc").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].title").value("Operating Systems"))
                .andExpect(jsonPath("$.totalItems").value(4))
                .andExpect(jsonPath("$.totalPages").value(2));

        mvc.perform(get("/api/admin/courses").session(admin).param("size", "2").param("page", "1").param("sort", "courseId"))
                .andExpect(jsonPath("$.items[*].courseId", contains("CSE301", "MTH101")));

        mvc.perform(get("/api/admin/courses").session(admin).param("q", "linear"))
                .andExpect(jsonPath("$.items[*].courseId", contains("MTH101")));

        mvc.perform(get("/api/admin/courses").session(admin).param("sort", "nope"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.allowed", hasItem("title")));
    }

    @Test
    void catalogShowsSeatsInstructorAndSchedule() throws Exception {
        mvc.perform(get("/api/catalog/sections").session(login("stu2", "Stu@123"))
                        .param("semester", "Monsoon").param("year", "2026").param("sort", "courseId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(4))
                .andExpect(jsonPath("$.items[1].courseId").value("CSE201"))
                .andExpect(jsonPath("$.items[1].prerequisites", contains("CSE101")))
                .andExpect(jsonPath("$.items[1].instructorName").value("Rohan Mehta"))
                .andExpect(jsonPath("$.items[1].seatsLeft").value(0))
                .andExpect(jsonPath("$.items[1].open").value(false))
                .andExpect(jsonPath("$.items[1].schedule", hasSize(2)))
                .andExpect(jsonPath("$.items[2].open").value(true));
    }

    @Test
    void studentRegistrationsIncludeDroppedSections() throws Exception {
        MockHttpSession stu1 = login("stu1", "Stu@123");
        mvc.perform(post("/api/student/{uid}/drop", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"sectionId\":\"CSE201-INS001-26-1\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/student/{uid}/registrations", STU1).session(stu1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.courseId == 'CSE201')].status", contains("dropped")))
                .andExpect(jsonPath("$[?(@.courseId == 'CSE201')].canDrop", contains(false)))
                .andExpect(jsonPath("$[?(@.courseId == 'CSE101')].canDrop", contains(true)))
                .andExpect(jsonPath("$[?(@.courseId == 'CSE101')].instructorName", contains("Rohan Mehta")));
    }

    @Test
    void instructorSeesClassStatistics() throws Exception {
        MockHttpSession inst1 = login("inst1", "Inst@123");
        mvc.perform(post("/api/instructor/INS001/sections/CSE101-INS001-26-1/grades/compute").session(inst1))
                .andExpect(status().isOk());

        mvc.perform(get("/api/instructor/INS001/sections/CSE101-INS001-26-1/stats").session(inst1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolled").value(1))
                .andExpect(jsonPath("$.components[?(@.name == 'Quiz')].average", contains(17.0)))
                .andExpect(jsonPath("$.components[?(@.name == 'Quiz')].averagePercent", contains(85.0)))
                .andExpect(jsonPath("$.finalGrades.computed").value(1))
                .andExpect(jsonPath("$.finalGrades.average").value(81.6))
                .andExpect(jsonPath("$.finalGrades.distribution.A-").value(1))
                .andExpect(jsonPath("$.finalGrades.distribution.F").value(0));

        // An ungraded section reports nulls rather than zeros
        mvc.perform(get("/api/instructor/INS001/sections/CSE201-INS001-26-1/stats").session(inst1))
                .andExpect(jsonPath("$.enrolled").value(2))
                .andExpect(jsonPath("$.finalGrades.average").value(nullValue()));

        mvc.perform(get("/api/instructor/INS001/sections/MTH101-INS002-26-1/stats").session(inst1))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDashboardCounts() throws Exception {
        mvc.perform(get("/api/admin/dashboard").session(login("admin1", "Admin@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students").value(2))
                .andExpect(jsonPath("$.instructors").value(2))
                .andExpect(jsonPath("$.courses").value(4))
                .andExpect(jsonPath("$.activeSections").value(4))
                .andExpect(jsonPath("$.fullSections").value(1))
                .andExpect(jsonPath("$.activeEnrollments").value(3))
                .andExpect(jsonPath("$.lockedAccounts").value(0))
                .andExpect(jsonPath("$.maintenanceOn").value(false));
    }

    @Test
    void maintenanceStatusIsPublicForTheLoginBanner() throws Exception {
        mvc.perform(get("/api/system/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenanceOn").value(false));
    }

    @Test
    void openApiDocsArePublished() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/dashboard']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/change-password']").exists());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void badParametersGiveA400NotA500() throws Exception {
        MockHttpSession stu1 = login("stu1", "Stu@123");
        mvc.perform(get("/api/student/{uid}/timetable", STU1).session(stu1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.REQUEST_VALIDATION_FAILED.getCode()));
        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
