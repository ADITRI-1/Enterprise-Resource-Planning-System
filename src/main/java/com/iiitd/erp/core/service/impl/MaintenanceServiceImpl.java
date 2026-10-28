package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.core.service.NotificationService;
import com.iiitd.erp.core.service.MaintenanceState;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Setting;
import com.iiitd.erp.persistent.persistence.repository.SettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MaintenanceServiceImpl implements MaintenanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceServiceImpl.class);
    private static final String MAINTENANCE_KEY = "maintenanceMode";

    private final NotificationService notifications;

    private final SettingRepository settings;

    public MaintenanceServiceImpl(SettingRepository settings, NotificationService notifications) {
        this.notifications = notifications;
        this.settings = settings;
    }

    @Override
    public boolean isReadOnly() {
        return loadFlag();
    }

    @Override
    public MaintenanceState status() {
        boolean on = loadFlag();
        return new MaintenanceState(on, buildBanner(on));
    }

    @Override
    @Transactional
    public MaintenanceState updateMaintenanceMode(boolean maintenanceOn) {
        LOGGER.info("Updating maintenance mode to {}", maintenanceOn);
        try {
            boolean wasOn = loadFlag();
            Setting row = settings.findById(MAINTENANCE_KEY)
                    .orElseGet(() -> new Setting(MAINTENANCE_KEY, Boolean.toString(maintenanceOn)));
            row.setValue(Boolean.toString(maintenanceOn));
            settings.save(row);
            if (wasOn != maintenanceOn) {
                notifications.broadcast(NotificationService.MAINTENANCE,
                        maintenanceOn ? "Maintenance started" : "Maintenance finished",
                        maintenanceOn
                                ? "The system is read-only for now. You can view everything, but changes are paused."
                                : "Everything is back to normal. You can register, drop and grade again.",
                        null);
            }
            return new MaintenanceState(maintenanceOn, buildBanner(maintenanceOn));
        } catch (ErpException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Failed to flip maintenance flag", ex);
            throw ErpException.wrap(ErrorCode.MAINTENANCE_UPDATE_FAILED,
                    ex, Map.of("maintenanceOn", maintenanceOn));
        }
    }

    private boolean loadFlag() {
        try {
            return settings.findById(MAINTENANCE_KEY)
                    .map(setting -> Boolean.parseBoolean(setting.getValue()))
                    .orElse(false);
        } catch (Exception ex) {
            LOGGER.error("Failed to read maintenance flag", ex);
            throw ErpException.wrap(ErrorCode.MAINTENANCE_STATUS_FETCH_FAILED,
                    ex, Map.of("key", MAINTENANCE_KEY));
        }
    }

    private String buildBanner(boolean enabled) {
        return enabled
                ? "System is in maintenance mode. You can view data, but changes are blocked."
                : "";
    }
}
