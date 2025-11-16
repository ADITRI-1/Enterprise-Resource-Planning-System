package com.iiitd.erp.io.dto.response;

import lombok.Builder;

import java.util.UUID;


@Builder
public record RoomAvailabilityResponse(String roomId, String roomName, int capacity) {}
