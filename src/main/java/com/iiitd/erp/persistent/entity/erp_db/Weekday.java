package com.iiitd.erp.persistent.entity.erp_db;

import java.time.DayOfWeek;

public enum Weekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    public static Weekday from(DayOfWeek day) {
        return valueOf(day.name());
    }

    public DayOfWeek toJava() {
        return DayOfWeek.valueOf(name());
    }
}
