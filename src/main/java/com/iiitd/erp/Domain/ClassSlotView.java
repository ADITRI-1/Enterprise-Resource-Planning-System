package com.iiitd.erp.Domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ClassSlotView(DayOfWeek day, LocalTime startTime, LocalTime endTime, String roomId) {}
