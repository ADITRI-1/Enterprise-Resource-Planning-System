package com.iiitd.erp.core.service.dto;

import com.iiitd.erp.Domain.ClassScheduleDomain;
import com.iiitd.erp.Domain.SectionDomain;

import java.util.List;

public record SectionScheduleResult(SectionDomain section, List<ClassScheduleDomain> slots) {}
