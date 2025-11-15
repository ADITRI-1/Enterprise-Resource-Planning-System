package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.*;
import com.iiitd.erp.core.service.*;
import com.iiitd.erp.io.dto.request.*;
import com.iiitd.erp.io.dto.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final MaintenanceService maintenance;

    public SystemController(MaintenanceService maintenance) {
        this.maintenance = maintenance;
    }

    @GetMapping("/maintenance")
    public ResponseEntity<MaintenanceStatusResponse> maintenanceStatus() {
        MaintenanceState state = maintenance.status();
        return ResponseEntity.ok(new MaintenanceStatusResponse(state.maintenanceOn(), state.bannerMessage()));
    }
}

