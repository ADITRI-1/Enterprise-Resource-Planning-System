package com.iiitd.erp.core.service;

import java.time.LocalTime;

public interface WorkingHoursService {
    LocalTime start();
    LocalTime end();
    void ensureWithin(LocalTime start, LocalTime end);
}
