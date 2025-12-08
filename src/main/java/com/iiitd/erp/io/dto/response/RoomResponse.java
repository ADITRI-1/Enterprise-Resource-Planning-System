package com.iiitd.erp.io.dto.response;

import lombok.Builder;

@Builder
public record RoomResponse(
        String roomId,
        String building,
        Integer capacity,
        String type
) {}
