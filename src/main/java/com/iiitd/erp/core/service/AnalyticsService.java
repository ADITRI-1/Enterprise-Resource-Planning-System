package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.AnalyticsView;

public interface AnalyticsService {
    AnalyticsView adminAnalytics(String semester, Integer year);
}
