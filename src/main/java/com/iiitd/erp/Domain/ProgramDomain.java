package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@Builder
@With
public class ProgramDomain {
    private String programId;
    private String programDesc;
    private String programType;
    private Integer duration;
}
