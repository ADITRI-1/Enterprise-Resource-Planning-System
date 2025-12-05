//package com.iiitd.erp.io.controller;
//
//import com.iiitd.erp.core.service.*;
//import com.iiitd.erp.exception.ErrorCode;
//import com.iiitd.erp.exception.ErpException;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class AdminControllerTest {
//
//    @Mock private MaintenanceService maintenanceService;
//    @Mock private CourseService courseService;
//    @Mock private DepartmentService departmentService;
//    @Mock private ProgramService programService;
//    @Mock private RoomService roomService;
//    @Mock private StudentService studentService;
//    @Mock private InstructorService instructorService;
//
//    @InjectMocks private AdminController controller;
//
//    @Test
//    void maintenanceStatus_success() {
//        when(maintenanceService.status()).thenReturn(new MaintenanceState(false, "System operational"));
//        ResponseEntity<?> resp = controller.status();
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//    }
//
//    @Test
//    void maintenanceStatus_failure() {
//        when(maintenanceService.status()).thenThrow(ErpException.of(ErrorCode.MAINTENANCE_STATUS_FETCH_FAILED, Map.of()));
//        ResponseEntity<?> resp = controller.status();
//        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
//    }
//
//    @Test
//    void toggleMaintenance_success() {
//        ResponseEntity<?> resp = controller.toggle(new com.iiitd.erp.io.dto.request.MaintenanceToggleRequest(true));
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//    }
//
//    @Test
//    void toggleMaintenance_failure() {
//        when(maintenanceService.setReadOnly(true)).thenThrow(ErpException.of(ErrorCode.MAINTENANCE_UPDATE_FAILED, Map.of()));
//        ResponseEntity<?> resp = controller.toggle(new com.iiitd.erp.io.dto.request.MaintenanceToggleRequest(true));
//        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
//    }
//}
