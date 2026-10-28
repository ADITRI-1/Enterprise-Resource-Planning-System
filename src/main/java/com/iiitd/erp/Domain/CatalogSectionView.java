package com.iiitd.erp.Domain;

import java.time.LocalDate;
import java.util.List;

/** One registrable section as shown in the student course catalog. */
public record CatalogSectionView(String sectionId,
                                 String courseId,
                                 String title,
                                 int credits,
                                 String deptId,
                                 String instructorCode,
                                 String instructorName,
                                 String semester,
                                 int year,
                                 int capacity,
                                 int enrolled,
                                 int seatsLeft,
                                 LocalDate registrationDeadline,
                                 boolean open,
                                 List<ClassSlotView> schedule) {}
