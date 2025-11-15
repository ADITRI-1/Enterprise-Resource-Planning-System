package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.RoomDomain;

import java.util.List;

public interface RoomService {
    RoomDomain createRoom(RoomDomain roomDomain);
    RoomDomain updateRoom(RoomDomain roomDomain);
    boolean deleteRoom(String roomId);
    List<RoomDomain> listRooms();
}
