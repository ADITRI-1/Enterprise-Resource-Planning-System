package com.iiitd.erp.integration;

import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Prerequisites, timetable clashes, notifications, GPA/transcript, audit log and analytics. */
class Phase3IntegrationTest extends PostgresIntegrationTest {

    private static final String STU1 = "33333333-3333-3333-3333-000000000001";
    private static final String STU1_CSE201_ENROLLMENT = "44444444-4444-4444-4444-000000000002";

    private String section(String id) {
        return "{\"sectionId\":\"" + id + "\"}";
    }

    @Test
    void prerequisitesMustBePassedBeforeRegistering() throws Exception {
        MockHttpSession stu1 = login("stu1", "Stu@123");
        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(section("CSE301-INS001-26-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PREREQUISITE_NOT_MET.getCode()))
                .andExpect(jsonPath("$.details.missing", contains("CSE201")));

        // An F doesn't count as passing; a B does.
        jdbc.update("insert into erp_db.grades (enrollment_id, component, score, final_grade) values (?::uuid, 'FINAL', 20, 'F')",
                STU1_CSE201_ENROLLMENT);
        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(section("CSE301-INS001-26-1")))
                .andExpect(status().isBadRequest());
        jdbc.update("update erp_db.grades set final_grade = 'B', score = 72 where enrollment_id = ?::uuid", STU1_CSE201_ENROLLMENT);
        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(section("CSE301-INS001-26-1")))
                .andExpect(status().isOk());
    }

    @Test
    void overlappingClassesAreRejected() throws Exception {
        // Move MTH101's Monday class onto CSE101's Monday 09:00-10:30 slot (different room).
        jdbc.update("update erp_db.class_schedules set start_time = '09:30', end_time = '10:30' "
                + "where section_id = 'MTH101-INS002-26-1' and day_of_week = 'MONDAY'");
        mvc.perform(post("/api/student/{uid}/register", STU1).session(login("stu1", "Stu@123"))
                        .contentType(MediaType.APPLICATION_JSON).content(section("MTH101-INS002-26-1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.TIMETABLE_CLASH.getCode()))
                .andExpect(jsonPath("$.details.conflictsWith").value("CSE101"))
                .andExpect(jsonPath("$.details.day").value("MONDAY"));
    }

    @Test
    void adminManagesPrerequisitesWithoutCycles() throws Exception {
        MockHttpSession admin = login("admin1", "Admin@123");
        // CSE301 -> CSE201 -> CSE101, so CSE101 can't require CSE301.
        mvc.perform(put("/api/admin/courses/CSE101/prerequisites").session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"prerequisites\":[\"CSE301\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PREREQUISITE_INVALID.getCode()));
        mvc.perform(put("/api/admin/courses/CSE301/prerequisites").session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"prerequisites\":[\"NOPE999\"]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/admin/courses/CSE301/prerequisites").session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"prerequisites\":[\"MTH101\",\"CSE201\"]}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/prerequisites").session(admin))
                .andExpect(jsonPath("$.CSE301", contains("CSE201", "MTH101")))
                .andExpect(jsonPath("$.CSE201", contains("CSE101")));
    }

    @Test
    void notificationsForRegistrationGradesAndMaintenance() throws Exception {
        MockHttpSession stu1 = login("stu1", "Stu@123");
        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(section("MTH101-INS002-26-1")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/instructor/INS001/sections/CSE101-INS001-26-1/grades/compute").session(login("inst1", "Inst@123")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/notifications/unread-count").session(stu1)).andExpect(jsonPath("$.count").value(2));
        String body = mvc.perform(get("/api/notifications").session(stu1))
                .andExpect(jsonPath("$.items[*].title", containsInAnyOrder("Final grade published for CSE101", "Registered for MTH101")))
                .andExpect(jsonPath("$.items[?(@.type == 'GRADES')].message", contains("Your final grade is A- (81.60 / 100).")))
                .andReturn().getResponse().getContentAsString();
        String firstId = com.jayway.jsonpath.JsonPath.read(body, "$.items[0].notificationId");
        mvc.perform(post("/api/notifications/{id}/read", firstId).session(stu1)).andExpect(status().isOk());
        mvc.perform(get("/api/notifications/unread-count").session(stu1)).andExpect(jsonPath("$.count").value(1));

        // Someone else's notification can't be marked
        mvc.perform(post("/api/notifications/{id}/read", firstId).session(login("stu2", "Stu@123")))
                .andExpect(status().isNotFound());

        MockHttpSession admin = login("admin1", "Admin@123");
        mvc.perform(put("/api/admin/settings/maintenance").session(admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"maintenanceOn\":true}")).andExpect(status().isOk());
        // Turning it on again doesn't announce twice
        mvc.perform(put("/api/admin/settings/maintenance").session(admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"maintenanceOn\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/notifications").session(login("stu2", "Stu@123")).param("unreadOnly", "true"))
                .andExpect(jsonPath("$.items[*].title", contains("Maintenance started")));

        // Reading notifications still works while maintenance blocks other writes
        mvc.perform(post("/api/notifications/read-all").session(stu1)).andExpect(status().isOk());
        mvc.perform(get("/api/notifications/unread-count").session(stu1)).andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void academicRecordAndPdfTranscript() throws Exception {
        mvc.perform(post("/api/instructor/INS001/sections/CSE101-INS001-26-1/grades/compute").session(login("inst1", "Inst@123")))
                .andExpect(status().isOk());
        MockHttpSession stu1 = login("stu1", "Stu@123");

        mvc.perform(get("/api/student/{uid}/academic-record", STU1).session(stu1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.terms", hasSize(1)))
                .andExpect(jsonPath("$.terms[0].semester").value("MONSOON"))
                .andExpect(jsonPath("$.terms[0].courses[?(@.courseId == 'CSE101')].grade", contains("A-")))
                .andExpect(jsonPath("$.terms[0].courses[?(@.courseId == 'CSE201')].grade", contains(nullValue())))
                .andExpect(jsonPath("$.terms[0].sgpa").value(9.0))
                .andExpect(jsonPath("$.cgpa").value(9.0))
                .andExpect(jsonPath("$.creditsEarned").value(4));

        byte[] pdf = mvc.perform(get("/api/student/{uid}/transcript.pdf", STU1).session(stu1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        assertThat(pdf.length).isGreaterThan(1000);

        mvc.perform(get("/api/student/{uid}/transcript.pdf", STU1).session(login("stu2", "Stu@123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditLogRecordsWritesAndLoginAttempts() throws Exception {
        attemptLogin("stu1", "wrong").andExpect(status().isUnauthorized());
        MockHttpSession stu1 = login("stu1", "Stu@123");
        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                .contentType(MediaType.APPLICATION_JSON).content(section("MTH101-INS002-26-1"))).andExpect(status().isOk());
        jdbc.update("update erp_db.settings set value = 'true' where key = 'maintenanceMode'");
        mvc.perform(post("/api/student/{uid}/drop", STU1).session(stu1)
                .contentType(MediaType.APPLICATION_JSON).content(section("MTH101-INS002-26-1"))).andExpect(status().isServiceUnavailable());

        MockHttpSession admin = login("admin1", "Admin@123");
        mvc.perform(get("/api/admin/audit").session(admin).param("q", "stu1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].path").value("/api/student/" + STU1 + "/drop"))
                .andExpect(jsonPath("$.items[0].status").value(503))
                .andExpect(jsonPath("$.items[1].path").value("/api/student/" + STU1 + "/register"))
                .andExpect(jsonPath("$.items[1].role").value("student"))
                .andExpect(jsonPath("$.items[?(@.path == '/api/auth/login' && @.status == 401)].actor", contains("stu1")))
                .andExpect(jsonPath("$.totalItems").value(4));

        // Without a filter (what the page sends first)
        mvc.perform(get("/api/admin/audit").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].actor").value("admin1"))
                .andExpect(jsonPath("$.items[0].path").value("/api/auth/login"));

        mvc.perform(get("/api/admin/audit").session(stu1)).andExpect(status().isForbidden());
    }

    @Test
    void adminAnalytics() throws Exception {
        mvc.perform(post("/api/instructor/INS001/sections/CSE101-INS001-26-1/grades/compute").session(login("inst1", "Inst@123")));
        mvc.perform(get("/api/admin/analytics").session(login("admin1", "Admin@123"))
                        .param("semester", "Monsoon").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionFill", hasSize(4)))
                .andExpect(jsonPath("$.sectionFill[0].sectionId").value("CSE201-INS001-26-1"))
                .andExpect(jsonPath("$.enrollmentsByDepartment[0].deptId").value("CSE"))
                .andExpect(jsonPath("$.enrollmentsByDepartment[0].enrolled").value(3))
                .andExpect(jsonPath("$.gradeDistribution.A-").value(1))
                .andExpect(jsonPath("$.gradeDistribution.F").value(0))
                .andExpect(jsonPath("$.registrationsByDay[0].registrations").value(3));
    }
}
