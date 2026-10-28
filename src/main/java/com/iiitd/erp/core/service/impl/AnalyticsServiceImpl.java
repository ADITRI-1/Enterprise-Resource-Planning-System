package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.AnalyticsView;
import com.iiitd.erp.core.service.AnalyticsService;
import com.iiitd.erp.core.service.support.GradeScale;
import com.iiitd.erp.persistent.persistence.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int REGISTRATION_WINDOW_DAYS = 30;

    private final AnalyticsRepository analytics;

    public AnalyticsServiceImpl(AnalyticsRepository analytics) {
        this.analytics = analytics;
    }

    @Override
    public AnalyticsView adminAnalytics(String semester, Integer year) {
        String term = semester == null || semester.isBlank() ? null : semester.trim().toUpperCase(Locale.ROOT);
        Map<String, Long> counts = analytics.gradeCounts(term, year);
        // Every letter appears (in order) so charts have a stable x-axis.
        Map<String, Long> distribution = new LinkedHashMap<>();
        GradeScale.letters().forEach(letter -> distribution.put(letter, counts.getOrDefault(letter, 0L)));
        return new AnalyticsView(
                analytics.sectionFill(term, year),
                analytics.enrollmentsByDepartment(term, year),
                distribution,
                analytics.registrationsByDay(REGISTRATION_WINDOW_DAYS));
    }
}
