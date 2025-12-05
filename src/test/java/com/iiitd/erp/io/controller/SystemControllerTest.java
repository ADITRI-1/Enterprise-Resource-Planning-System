//package com.iiitd.erp.io.controller;
//
//import com.iiitd.erp.core.service.MaintenanceService;
//import com.iiitd.erp.core.service.MaintenanceState;
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
//class SystemControllerTest {
//
//    @Mock private MaintenanceService maintenanceService;
//    @InjectMocks private SystemController controller;
//
//    @Test
//    void maintenanceStatus_success() {
//        when(maintenanceService.status()).thenReturn(new MaintenanceState(false, "System operational"));
//        ResponseEntity<?> resp = controller.maintenanceStatus();
//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//    }
//
//    @Test
//    void maintenanceStatus_failure() {
//        when(maintenanceService.status()).thenThrow(ErpException.of(ErrorCode.MAINTENANCE_STATUS_FETCH_FAILED, Map.of()));
//        ResponseEntity<?> resp = controller.maintenanceStatus();
//        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
//    }
//}
