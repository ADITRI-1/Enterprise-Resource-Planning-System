package com.iiitd.erp.core.service;

public interface MaintenanceService {
    boolean isReadOnly();
    MaintenanceState status();
    MaintenanceState updateMaintenanceMode(boolean maintenanceOn);
}
