package com.iiitd.erp.integration;

import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ErpFlowIntegrationTest extends PostgresIntegrationTest {

    private static final String STU1 = "33333333-3333-3333-3333-000000000001";
    private static final String STU2 = "33333333-3333-3333-3333-000000000002";
    private static final String CSE101 = "CSE101-INS001-26-1";
    private static final String CSE201 = "CSE201-INS001-26-1";
    private static final String MTH101 = "MTH101-INS002-26-1";

    @Autowired private SectionService sectionService;

    private String register(String sectionId) {
        return "{\"sectionId\":\"%s\"}".formatted(sectionId);
    }

    // ------------------------------------------------------------ auth

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"stu1\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect username or password"));
    }

    @Test
    void unauthenticatedRequestGets401() throws Exception {
        mvc.perform(get("/api/student/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void meEndpointsResolveProfiles() throws Exception {
        mvc.perform(get("/api/student/me").session(login("stu1", "Stu@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(STU1))
                .andExpect(jsonPath("$.rollNo").value("2024001"));

        mvc.perform(get("/api/instructor/me").session(login("inst1", "Inst@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructorCode").value("INS001"));
    }

    // ------------------------------------------------------------ student

    @Test
    void studentRegisterDuplicateDropAndReRegister() throws Exception {
        MockHttpSession stu1 = login("stu1", "Stu@123");

        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(register(MTH101)))
                .andExpect(status().isOk());

        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(register(MTH101)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ALREADY_ENROLLED.getCode()));

        mvc.perform(post("/api/student/{uid}/drop", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(register(MTH101)))
                .andExpect(status().isOk());
        assertThat(enrollmentCount(MTH101)).isZero();

        // Re-registering after a drop reuses the (student, section) row instead of violating the unique key.
        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(register(MTH101)))
                .andExpect(status().isOk());
        assertThat(enrollmentCount(MTH101)).isEqualTo(1);
    }

    @Test
    void fullSectionIsBlocked() throws Exception {
        // CSE201 is seeded at capacity 2 with both students; free a seat is not possible, so shrink CSE101.
        jdbc.update("update erp_db.sections set capacity = 1 where section_id = ?", CSE101);

        mvc.perform(post("/api/student/{uid}/register", STU2).session(login("stu2", "Stu@123"))
                        .contentType(MediaType.APPLICATION_JSON).content(register(CSE101)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Section full"));
    }

    @Test
    void registrationAfterDeadlineIsBlocked() throws Exception {
        jdbc.update("update erp_db.sections set registration_deadline = date '2020-01-01' where section_id = ?", MTH101);

        mvc.perform(post("/api/student/{uid}/register", STU1).session(login("stu1", "Stu@123"))
                        .contentType(MediaType.APPLICATION_JSON).content(register(MTH101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.REGISTRATION_DEADLINE_PASSED.getCode()));
    }

    @Test
    void studentCannotTouchAnotherStudentsRecords() throws Exception {
        MockHttpSession stu1 = login("stu1", "Stu@123");
        mvc.perform(get("/api/student/{uid}/grades", STU2).session(stu1)).andExpect(status().isForbidden());
        mvc.perform(post("/api/student/{uid}/drop", STU2).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(register(CSE201)))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentTimetableAndTranscript() throws Exception {
        MockHttpSession stu1 = login("stu1", "Stu@123");
        mvc.perform(get("/api/student/{uid}/timetable", STU1).session(stu1)
                        .param("semester", "Monsoon").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        mvc.perform(get("/api/student/{uid}/transcript.csv", STU1).session(stu1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"CSE101\"")));
    }

    @Test
    void lastSeatCannotBeTakenTwiceConcurrently() throws Exception {
        jdbc.update("update erp_db.sections set capacity = 1 where section_id = ?", MTH101);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (String student : List.of(STU1, STU2)) {
            results.add(pool.submit(() -> {
                start.await();
                try {
                    sectionService.registerStudent(student, MTH101);
                    return true;
                } catch (ErpException ex) {
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.SECTION_FULL);
                    return false;
                }
            }));
        }
        start.countDown();
        int successes = 0;
        for (Future<Boolean> r : results) {
            if (r.get(30, TimeUnit.SECONDS)) successes++;
        }
        pool.shutdown();

        assertThat(successes).isEqualTo(1);
        assertThat(enrollmentCount(MTH101)).isEqualTo(1);
    }

    // ------------------------------------------------------------ instructor

    @Test
    void instructorComputesFinalGradesAndStudentSeesThem() throws Exception {
        MockHttpSession inst1 = login("inst1", "Inst@123");

        // 17/20*20 + 41/50*30 + 80/100*50 = 17 + 24.6 + 40 = 81.6 -> A-
        mvc.perform(post("/api/instructor/INS001/sections/{s}/grades/compute", CSE101).session(inst1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rollNo").value("2024001"))
                .andExpect(jsonPath("$[0].totalScore").value(81.6))
                .andExpect(jsonPath("$[0].finalGrade").value("A-"));

        mvc.perform(get("/api/instructor/INS001/sections/{s}/grades", CSE101).session(inst1))
                .andExpect(jsonPath("$[0].finalGrade").value("A-"));

        mvc.perform(get("/api/student/{uid}/grades", STU1).session(login("stu1", "Stu@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.component == 'FINAL')].finalGrade", contains("A-")));
    }

    @Test
    void instructorCannotGradeAnotherInstructorsSection() throws Exception {
        MockHttpSession inst1 = login("inst1", "Inst@123");

        // Using someone else's code in the URL
        mvc.perform(post("/api/instructor/INS002/sections/{s}/grades/compute", MTH101).session(inst1))
                .andExpect(status().isForbidden());
        // Using their own code against a section they don't teach
        mvc.perform(post("/api/instructor/INS001/sections/{s}/grades/compute", MTH101).session(inst1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Not your section"));
    }

    @Test
    void computeRequiresWeightsToTotal100() throws Exception {
        jdbc.update("update erp_db.assessment_components set weight = 0.1 where name = 'Quiz' and section_id = ?", CSE101);

        mvc.perform(post("/api/instructor/INS001/sections/{s}/grades/compute", CSE101).session(login("inst1", "Inst@123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ASSESSMENT_WEIGHTS_INCOMPLETE.getCode()));
    }

    @Test
    void scoreCsvExportCanBeImportedBack() throws Exception {
        MockHttpSession inst1 = login("inst1", "Inst@123");
        String csv = mvc.perform(get("/api/instructor/INS001/sections/{s}/scores/export", CSE101).session(inst1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Quiz\"").contains("17.0");
        String edited = csv.replace("17.0", "19.0");
        mvc.perform(multipart("/api/instructor/INS001/sections/{s}/scores/import", CSE101)
                        .file(new MockMultipartFile("file", "scores.csv", "text/csv", edited.getBytes(StandardCharsets.UTF_8)))
                        .session(inst1))
                .andExpect(status().isOk());

        Double quiz = jdbc.queryForObject("""
                select sc.score from erp_db.assessment_scores sc
                join erp_db.assessment_components c on c.component_id = sc.component_id
                where c.name = 'Quiz' and c.section_id = ?""", Double.class, CSE101);
        assertThat(quiz).isEqualTo(19.0);
    }

    // ------------------------------------------------------------ maintenance

    @Test
    void maintenanceModeBlocksStudentAndInstructorWrites() throws Exception {
        MockHttpSession admin = login("admin1", "Admin@123");
        MockHttpSession stu1 = login("stu1", "Stu@123");
        MockHttpSession inst1 = login("inst1", "Inst@123");

        mvc.perform(put("/api/admin/settings/maintenance").session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"maintenanceOn\":true}"))
                .andExpect(status().isOk());

        try {
            mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                            .contentType(MediaType.APPLICATION_JSON).content(register(MTH101)))
                    .andExpect(status().isServiceUnavailable());
            mvc.perform(post("/api/instructor/INS001/sections/{s}/grades/compute", CSE101).session(inst1))
                    .andExpect(status().isServiceUnavailable());
            // Reads still work
            mvc.perform(get("/api/student/{uid}/grades", STU1).session(stu1)).andExpect(status().isOk());
        } finally {
            mvc.perform(put("/api/admin/settings/maintenance").session(admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"maintenanceOn\":false}"))
                    .andExpect(status().isOk());
        }

        mvc.perform(post("/api/student/{uid}/register", STU1).session(stu1)
                        .contentType(MediaType.APPLICATION_JSON).content(register(MTH101)))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------ admin

    @Test
    void adminSetsUpCourseSectionScheduleAndStudent() throws Exception {
        MockHttpSession admin = login("admin1", "Admin@123");

        mvc.perform(post("/api/admin/courses").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":\"CSE999\",\"title\":\"Compilers\",\"deptId\":\"CSE\",\"credits\":4}"))
                .andExpect(status().is2xxSuccessful());

        mvc.perform(post("/api/admin/courses/CSE999/sections").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"instructorCode":"INS002","semester":"Monsoon","year":2026,
                                 "capacity":30,"registrationDeadline":"2027-01-31"}"""))
                .andExpect(status().is2xxSuccessful());
        String sectionId = "CSE999-INS002-26-1";

        // C101 is taken by CSE101 on Monday 09:00-10:30
        mvc.perform(put("/api/admin/sections/{s}/schedule", sectionId).session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slots\":[{\"day\":\"MONDAY\",\"startTime\":\"09:30\",\"endTime\":\"10:30\",\"roomId\":\"C101\"}]}"))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/admin/sections/{s}/schedule", sectionId).session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slots\":[{\"day\":\"TUESDAY\",\"startTime\":\"09:00\",\"endTime\":\"10:30\",\"roomId\":\"C101\"}]}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.slots[0].day").value("TUESDAY"));
        // Wall-clock time is stored as entered, regardless of the server's time zone
        assertThat(jdbc.queryForObject("select start_time::text from erp_db.class_schedules where section_id = ?",
                String.class, sectionId)).isEqualTo("09:00:00");

        String created = mvc.perform(post("/api/admin/create/student").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Kabir Singh","rollNo":2024003,"dateOfBirth":"05-11-2005",
                                 "programId":"BTECH-CSE","yearOfRegistration":"2024"}"""))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.username").value("kabir_bt2024003"))
                .andExpect(jsonPath("$.temporaryPassword").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String tempPassword = com.jayway.jsonpath.JsonPath.read(created, "$.temporaryPassword");

        // First login with the temporary password must change it before anything else works
        MockHttpSession kabir = login("kabir_bt2024003", tempPassword);
        mvc.perform(get("/api/student/me").session(kabir))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PASSWORD_CHANGE_REQUIRED.getCode()));
        mvc.perform(post("/api/auth/change-password").session(kabir).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"%s\",\"newPassword\":\"Kabir2024!\"}".formatted(tempPassword)))
                .andExpect(status().isOk());

        // Profile loads from the ERP DB via the user id shared with the Auth DB
        String uid = com.jayway.jsonpath.JsonPath.read(
                mvc.perform(get("/api/student/me").session(kabir)).andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(), "$.userId");
        mvc.perform(post("/api/student/{uid}/register", uid).session(kabir)
                        .contentType(MediaType.APPLICATION_JSON).content(register(sectionId)))
                .andExpect(status().isOk());

        // Only a hash is stored
        String hash = jdbc.queryForObject("select password_hash from auth_db.user_auth where username = 'kabir_bt2024003'", String.class);
        assertThat(hash).startsWith("$2a$").doesNotContain("Kabir2024!");
    }

    @Test
    void nonAdminCannotUseAdminApi() throws Exception {
        mvc.perform(get("/api/admin/students").session(login("stu1", "Stu@123")))
                .andExpect(status().isForbidden());
    }

    private int enrollmentCount(String sectionId) {
        return jdbc.queryForObject("select enrollment_count from erp_db.sections where section_id = ?",
                Integer.class, sectionId);
    }
}
