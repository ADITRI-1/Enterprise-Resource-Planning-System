package com.iiitd.erp.io.dto.request;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@With
public record RoomCreateRequest(
        String roomNumber,
        String building,
        Integer capacity,
        String type
) {}
