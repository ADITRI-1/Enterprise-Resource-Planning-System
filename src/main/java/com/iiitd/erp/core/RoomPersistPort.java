package com.iiitd.erp.core;

import com.iiitd.erp.Domain.RoomDomain;

import java.util.List;

public interface RoomPersistPort {
    RoomDomain findByRoomNumber(String roomNumber);
    RoomDomain findByRoomId(String roomId);
    RoomDomain save(RoomDomain roomDomain);
    RoomDomain update(RoomDomain roomDomain);
    boolean deleteByRoomId(String roomId);
    List<RoomDomain> findAll();
}
