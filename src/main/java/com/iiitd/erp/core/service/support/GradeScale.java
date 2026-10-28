package com.iiitd.erp.core.service.support;

import java.math.BigDecimal;

/**
 * Converts a weighted total (0-100) to a letter grade. Bands are inclusive lower bounds.
 */
public final class GradeScale {

    private static final int[] CUTOFFS = {90, 80, 70, 60, 50, 40, 30};
    private static final String[] LETTERS = {"A", "A-", "B", "B-", "C", "C-", "D"};

    private GradeScale() {}

    public static String letterFor(BigDecimal total) {
        double value = total == null ? 0 : total.doubleValue();
        for (int i = 0; i < CUTOFFS.length; i++) {
            if (value >= CUTOFFS[i]) {
                return LETTERS[i];
            }
        }
        return "F";
    }
}
