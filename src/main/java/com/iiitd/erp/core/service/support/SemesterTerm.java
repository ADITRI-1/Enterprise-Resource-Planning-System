package com.iiitd.erp.core.service.support;

import java.util.Locale;

public enum SemesterTerm {
    MONSOON("MONSOON", "1"),
    WINTER("WINTER", "2");

    private final String label;
    private final String marker;

    SemesterTerm(String label, String marker) {
        this.label = label;
        this.marker = marker;
    }

    public String label() { return label; }
    public String marker() { return marker; }

    public static SemesterTerm from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Semester is required");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (SemesterTerm term : values()) {
            if (term.label.equals(normalized)) {
                return term;
            }
        }
        throw new IllegalArgumentException("Unsupported semester: " + value);
    }
}
