package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.RegistrationOutcomeDomain;
import com.iiitd.erp.Domain.StudentGradeDomain;
import com.iiitd.erp.Domain.StudentTimeTableSlotDomain;
import com.iiitd.erp.core.service.CatalogService;
import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.RegisterRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock private SectionService sections;
    @Mock private MaintenanceService maintenance;
    @Mock private CatalogService catalog;
    @InjectMocks private StudentController controller;

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private void setAuth(String name, String role, boolean authenticated) {
        var auth = new TestingAuthenticationToken(
                name,
                "pwd",
                Collections.singleton(new SimpleGrantedAuthority(role)));
        auth.setAuthenticated(authenticated);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void register_success() {
        setAuth("uid1", "STUDENT", true);
        when(maintenance.isReadOnly()).thenReturn(false);
        when(sections.registerStudent("uid1", "SEC-1"))
                .thenReturn(RegistrationOutcomeDomain.builder().status("OK").message("ok").build());

        ResponseEntity<?> resp = controller.register("uid1", new RegisterRequest("SEC-1"));
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void register_forbidden_whenAuthMismatch() {
        setAuth("other", "STUDENT", true);
        ResponseEntity<?> resp = controller.register("uid1", new RegisterRequest("SEC-1"));
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void register_blockedByMaintenance() {
        setAuth("uid1", "STUDENT", true);
        when(maintenance.isReadOnly()).thenReturn(true);

        ResponseEntity<?> resp = controller.register("uid1", new RegisterRequest("SEC-1"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
    }

    @Test
    void register_erpFailure() {
        setAuth("uid1", "STUDENT", true);
        when(maintenance.isReadOnly()).thenReturn(false);
        when(sections.registerStudent("uid1", "SEC-1"))
                .thenThrow(ErpException.of(ErrorCode.STUDENT_REGISTRATION_BLOCKED, Map.of()));

        ResponseEntity<?> resp = controller.register("uid1", new RegisterRequest("SEC-1"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
    }

    @Test
    void timetable_success() {
        setAuth("uid1", "STUDENT", true);
        List<StudentTimeTableSlotDomain> slots = List.of(
                StudentTimeTableSlotDomain.builder()
                        .dayOfWeek(1).courseCode("C1").courseTitle("Title").room("R1")
                        .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(10,0))
                        .build());
        when(sections.timetableForStudent("uid1", "MONSOON", 2024)).thenReturn(slots);

        ResponseEntity<?> resp = controller.timetable("uid1", "MONSOON", 2024);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void grades_success() {
        setAuth("uid1", "STUDENT", true);
        List<StudentGradeDomain> grades = List.of(
                StudentGradeDomain.builder().courseCode("C1").courseTitle("T").component("MID").score(BigDecimal.valueOf(10.0)).build());
        when(sections.gradesForStudent("uid1")).thenReturn(grades);

        ResponseEntity<?> resp = controller.grades("uid1");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void transcriptDownload_missingFile() {
        setAuth("uid1", "STUDENT", true);
        when(sections.exportTranscript("uid1")).thenReturn(Path.of("nonexistent.csv"));

        ResponseEntity<?> resp = controller.transcriptDownload("uid1");
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }
}
