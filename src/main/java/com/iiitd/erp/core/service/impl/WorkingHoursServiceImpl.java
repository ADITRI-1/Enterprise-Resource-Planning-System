package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.core.service.WorkingHoursService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.persistence.repository.SettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Map;

import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Service
@Transactional(readOnly = true)
public class WorkingHoursServiceImpl implements WorkingHoursService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkingHoursServiceImpl.class);
    private static final String START_KEY = "workingHours.start";
    private static final String END_KEY   = "workingHours.end";
    private static final LocalTime DEFAULT_START = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_END   = LocalTime.of(20, 0);

    private final SettingRepository settings;

    public WorkingHoursServiceImpl(SettingRepository settings) {
        this.settings = settings;
    }

    @Override public LocalTime start() { return resolve(START_KEY, DEFAULT_START); }
    @Override public LocalTime end()   { return resolve(END_KEY, DEFAULT_END);  }

    @Override
    public void ensureWithin(LocalTime slotStart, LocalTime slotEnd) {
        LocalTime ws = start();
        LocalTime we = end();
        if (slotStart.isBefore(ws) || slotEnd.isAfter(we)) {
            throw ErpException.of(ErrorCode.SCHEDULE_OUTSIDE_WORKING_HOURS,
                    Map.of("workingHoursStart", ws, "workingHoursEnd", we,
                            "requestedStart", slotStart, "requestedEnd", slotEnd));
        }
    }

    private LocalTime resolve(String key, LocalTime fallback) {
        return settings.findById(key)
                .map(setting -> LocalTime.parse(setting.getValue()))
                .orElse(fallback);
    }
}
