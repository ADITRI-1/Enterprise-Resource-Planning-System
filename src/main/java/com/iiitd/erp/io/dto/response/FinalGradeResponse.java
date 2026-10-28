package com.iiitd.erp.io.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record FinalGradeResponse(UUID enrollmentId,
                                 UUID studentId,
                                 String rollNo,
                                 String studentName,
                                 BigDecimal totalScore,
                                 String finalGrade) {}
