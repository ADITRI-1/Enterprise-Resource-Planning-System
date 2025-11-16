package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, String> {
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByCapacityGreaterThanEqual(int capacity);

}
