package com.iiitd.erp.Domain;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@Builder
@With
public class RoomDomain {
    private String roomId;
    private String roomNumber;
    private String building;
    private Integer capacity;
    private String type;
}
