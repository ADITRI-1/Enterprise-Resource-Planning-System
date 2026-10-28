package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.RegistrationOutcomeDomain;
import com.iiitd.erp.Domain.StudentGradeDomain;
import com.iiitd.erp.core.service.AcademicRecordService;
import com.iiitd.erp.core.service.SectionService;
import com.iiitd.erp.core.service.StudentService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.io.controller.support.CurrentUser;
import com.iiitd.erp.io.dto.request.SectionIdRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock private SectionService sections;
    @Mock private StudentService students;
    @Mock private CurrentUser currentUser;
    @Mock private AcademicRecordService academicRecords;
    @InjectMocks private StudentController controller;

    @Test
    void register_success() {
        when(sections.registerStudent("uid1", "SEC-1"))
                .thenReturn(RegistrationOutcomeDomain.builder().status("OK").message("ok").build());

        assertThat(controller.register("uid1", new SectionIdRequest("SEC-1")).message()).isEqualTo("ok");
    }

    @Test
    void register_rejectedWhenNotOwnRecord() {
        doThrow(ErpException.of(ErrorCode.STUDENT_FORBIDDEN, Map.of())).when(currentUser).requireStudent("uid1");

        assertThatThrownBy(() -> controller.register("uid1", new SectionIdRequest("SEC-1")))
                .isInstanceOf(ErpException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.STUDENT_FORBIDDEN);
        verifyNoInteractions(sections);
    }

    @Test
    void grades_mapsRows() {
        when(sections.gradesForStudent("uid1")).thenReturn(List.of(
                StudentGradeDomain.builder().courseCode("C1").courseTitle("T").component("MID").score(BigDecimal.TEN).build()));

        assertThat(controller.grades("uid1")).singleElement()
                .satisfies(row -> assertThat(row.courseCode()).isEqualTo("C1"));
    }

    @Test
    void transcript_isCsvAttachment() {
        when(sections.exportTranscript("uid1")).thenReturn("CourseCode\n".getBytes());

        ResponseEntity<byte[]> resp = controller.transcript("uid1");
        assertThat(resp.getHeaders().getContentType().toString()).isEqualTo("text/csv");
        assertThat(resp.getHeaders().getContentDisposition().isAttachment()).isTrue();
    }
}
