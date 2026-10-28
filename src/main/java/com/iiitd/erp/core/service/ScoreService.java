package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.FinalGradeDomain;
import com.iiitd.erp.Domain.InstructorScoreOverviewDomain;
import com.iiitd.erp.io.dto.request.UpdateStudentScoresRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ScoreService {
    InstructorScoreOverviewDomain overview(String instructorCode, String sectionId);
    InstructorScoreOverviewDomain updateScores(String instructorCode, String sectionId,
                                         UpdateStudentScoresRequest request, String updatedBy);
    byte[] exportCsv(String instructorCode, String sectionId);
    InstructorScoreOverviewDomain importCsv(String instructorCode, String sectionId,
                                      MultipartFile file, String updatedBy);

    /** Applies the section's component weights to every enrolled student's scores and stores the letter grade. */
    List<FinalGradeDomain> computeFinalGrades(String instructorCode, String sectionId);
    List<FinalGradeDomain> finalGrades(String instructorCode, String sectionId);
}
