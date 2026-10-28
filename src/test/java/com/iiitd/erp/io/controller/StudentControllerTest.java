package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.RegistrationOutcomeDomain;
import com.iiitd.erp.Domain.StudentGradeDomain;
import com.iiitd.erp.Domain.StudentTimeTableSlotDomain;
import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.core.service.StudentService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.io.controller.support.CurrentUser;
import com.iiitd.erp.io.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock private SectionService sections;
    @Mock private MaintenanceService maintenance;
    @Mock private StudentService students;
    @Mock private CurrentUser currentUser;
    @InjectMocks private StudentController controller;

    @Test
    void register_success() {
        when(currentUser.isStudent("uid1")).thenReturn(true);
        when(maintenance.isReadOnly()).thenReturn(false);
        when(sections.registerStudent("uid1", "SEC-1"))
                .thenReturn(RegistrationOutcomeDomain.builder().status("OK").message("ok").build());

        ResponseEntity<?> resp = controller.register("uid1", new RegisterRequest("SEC-1"));
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void register_forbidden_whenNotOwnRecord() {
        when(currentUser.isStudent("uid1")).thenReturn(false);

        ResponseEntity<?> resp = controller.register("uid1", new RegisterRequest("SEC-1"));
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        verifyNoInteractions(sections);
    }

    @Test
    void register_blockedByMaintenance() {
        when(currentUser.isStudent("uid1")).thenReturn(true);
        when(maintenance.isReadOnly()).thenReturn(true);

        ResponseEntity<?> resp = controller.register("uid1", new RegisterRequest("SEC-1"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
    }

    @Test
    void register_sectionFull_isConflict() {
        when(currentUser.isStudent("uid1")).thenReturn(true);
        when(maintenance.isReadOnly()).thenReturn(false);
        when(sections.registerStudent("uid1", "SEC-1"))
                .thenThrow(ErpException.of(ErrorCode.SECTION_FULL, Map.of()));

        ResponseEntity<?> resp = controller.register("uid1", new RegisterRequest("SEC-1"));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    }

    @Test
    void timetable_success() {
        when(currentUser.isStudent("uid1")).thenReturn(true);
        List<StudentTimeTableSlotDomain> slots = List.of(
                StudentTimeTableSlotDomain.builder()
                        .dayOfWeek(1).courseCode("C1").courseTitle("Title").room("R1")
                        .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0))
                        .build());
        when(sections.timetableForStudent("uid1", "MONSOON", 2024)).thenReturn(slots);

        ResponseEntity<?> resp = controller.timetable("uid1", "MONSOON", 2024);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void grades_success() {
        when(currentUser.isStudent("uid1")).thenReturn(true);
        List<StudentGradeDomain> grades = List.of(
                StudentGradeDomain.builder().courseCode("C1").courseTitle("T").component("MID").score(BigDecimal.valueOf(10.0)).build());
        when(sections.gradesForStudent("uid1")).thenReturn(grades);

        ResponseEntity<?> resp = controller.grades("uid1");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void transcriptDownload_returnsCsv() {
        when(currentUser.isStudent("uid1")).thenReturn(true);
        when(sections.exportTranscript("uid1")).thenReturn("CourseCode\n".getBytes());

        ResponseEntity<?> resp = controller.transcriptDownload("uid1");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("text/csv", resp.getHeaders().getContentType().toString());
    }
}
