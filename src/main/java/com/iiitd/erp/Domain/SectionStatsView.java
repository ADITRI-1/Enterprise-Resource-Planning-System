package com.iiitd.erp.Domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Class statistics for an instructor's section. Averages are null when nothing is graded yet. */
public record SectionStatsView(String sectionId,
                               String courseId,
                               int enrolled,
                               List<ComponentStats> components,
                               FinalStats finalGrades) {

    public record ComponentStats(UUID componentId,
                                 String name,
                                 BigDecimal weight,
                                 int scoreOutOf,
                                 int graded,
                                 Double average,
                                 Double averagePercent,
                                 Double min,
                                 Double max,
                                 Double median) {}

    /** Distribution lists every letter (A … F) in order, including zero counts, for charting. */
    public record FinalStats(int computed,
                             Double average,
                             Double min,
                             Double max,
                             Map<String, Long> distribution) {}
}
