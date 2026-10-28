package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.AcademicRecordView;

public interface AcademicRecordService {
    AcademicRecordView record(String studentUserId);

    byte[] transcriptPdf(String studentUserId);
}
