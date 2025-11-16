package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.InstructorScoreOverviewDomain;
import com.iiitd.erp.io.dto.request.UpdateStudentScoresRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ScoreService {
    InstructorScoreOverviewDomain overview(String instructorCode, String sectionId);
    InstructorScoreOverviewDomain updateScores(String instructorCode, String sectionId,
                                         UpdateStudentScoresRequest request, String updatedBy);
    byte[] exportCsv(String instructorCode, String sectionId);
    InstructorScoreOverviewDomain importCsv(String instructorCode, String sectionId,
                                      MultipartFile file, String updatedBy);
}
