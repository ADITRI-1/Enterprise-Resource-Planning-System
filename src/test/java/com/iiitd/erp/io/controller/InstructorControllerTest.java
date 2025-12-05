//package com.iiitd.erp.io.controller;
//
//import com.iiitd.erp.Domain.AssessmentComponentDomain;
//import com.iiitd.erp.Domain.InstructorScoreOverviewDomain;
//import com.iiitd.erp.Domain.SectionDomain;
//import com.iiitd.erp.core.service.AssessmentService;
//import com.iiitd.erp.core.service.ScoreService;
//import com.iiitd.erp.core.service.SectionService;
//import com.iiitd.erp.core.service.TimetableService;
//import com.iiitd.erp.exception.ErrorCode;
//import com.iiitd.erp.exception.ErpException;
//import com.iiitd.erp.io.dto.request.AssessmentComponentRequest;
//import com.iiitd.erp.io.dto.request.UpdateStudentScoresRequest;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.TestingAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.math.BigDecimal;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class InstructorControllerTest {
//
//    @Mock private AssessmentService assessments;
//    @Mock private SectionService sectionService;
//    @Mock private TimetableService timetableService;
//    @Mock private ScoreService scoreService;
//
//    @InjectMocks private InstructorController controller;
//
//    @AfterEach void clearCtx() { SecurityContextHolder.clearContext(); }
//
//    private void setInstructorAuth(String name) {
//        var auth = new TestingAuthenticationToken(
//                name, "pwd", Collections.singleton(new SimpleGrantedAuthority("INSTRUCTOR")));
//        auth.setAuthenticated(true);
//        SecurityContextHolder.getContext().setAuthentication(auth);
//    }
//
////    @Test
////    void listSections_success() {
////        setInstructorAuth("I1");
////        when(sectionService.sectionsForInstructor("I1", "MONSOON", 2024))
////                .thenReturn(List.of(SectionDomain.builder()
////                        .sectionId("S1").courseId("C1").instructorCode("I1")
////                        .semester("MONSOON").year(2024).capacity(50).enrollmentCount(10).active(true)
////                        .build()));
////
////        ResponseEntity<?> resp = controller.listSections("I1", "MONSOON", 2024);
////        assertEquals(HttpStatus.OK, resp.getStatusCode());
////    }
//
////    @Test
////    void listSections_forbidden() {
////        setInstructorAuth("OTHER");
////        assertEquals(HttpStatus.FORBIDDEN,
////                controller.listSections("I1", "MONSOON", 2024).getStatusCode());
////    }
//
//    @Test
//    void createAssessment_success() {
//        setInstructorAuth("I1");
//        AssessmentComponentDomain created = AssessmentComponentDomain.builder()
//                .componentId(UUID.randomUUID()).sectionId("S1").name("Quiz").build();
//        when(assessments.create(eq("I1"), any())).thenReturn(created);
//
//        ResponseEntity<?> resp = controller.createAssessment("I1", "S1",
//                new AssessmentComponentRequest("Quiz",  0.2, 10, null));
//        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
//    }
//
//    @Test
//    void createAssessment_forbidden() {
//        setInstructorAuth("OTHER");
//        ResponseEntity<?> resp = controller.createAssessment("I1", "S1",
//                new AssessmentComponentRequest("Quiz", 0.2, 10, null));
//        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
//    }
//
//    @Test
//    void timetable_success() {
//        setInstructorAuth("I1");
//        when(timetableService.timetable("I1", "MONSOON", 2024)).thenReturn(List.of());
//        ResponseEntity<?> resp = controller.timetable("I1", "MONSOON", 2024);
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//    }
//
//    @Test
//    void updateScores_forbidden() {
//        setInstructorAuth("OTHER");
//        ResponseEntity<?> resp = controller.updateScores("I1", "S1", new UpdateStudentScoresRequest(List.of()));
//        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
//    }
//
//    @Test
//    void updateScores_success() {
//        setInstructorAuth("I1");
//        InstructorScoreOverviewDomain overview = InstructorScoreOverviewDomain.builder()
//                .section(SectionDomain.builder().sectionId("S1").build())
//                .components(List.of())
//                .students(List.of())
//                .build();
//        when(scoreService.updateScores(eq("I1"), eq("S1"), any(), anyString())).thenReturn(overview);
//
//        ResponseEntity<?> resp = controller.updateScores("I1", "S1", new UpdateStudentScoresRequest(List.of()));
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//    }
//
////    @Test
////    void exportScores_error() {
////        setInstructorAuth("I1");
////        when(scoreService.exportCsv(anyString(), anyString()))
////                .thenThrow(ErpException.of(ErrorCode.ASSESSMENT_SCORE_EXPORT_FAILED, Map.of()));
////        ResponseEntity<?> resp = controller.exportScores("I1", "S1");
////        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
////    }
//}
