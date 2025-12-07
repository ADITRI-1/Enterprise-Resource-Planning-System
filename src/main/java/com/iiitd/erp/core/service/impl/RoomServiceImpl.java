package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.RoomDomain;
import com.iiitd.erp.core.RoomPersistPort;
import com.iiitd.erp.core.service.RoomService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomPersistPort roomPersistPort;

    public RoomServiceImpl(RoomPersistPort roomPersistPort) {
        this.roomPersistPort = roomPersistPort;
    }

    @Override
    public RoomDomain createRoom(RoomDomain roomDomain) {
        String roomId = roomDomain.getRoomId();
        LOGGER.info("Creating room {}", roomId);

        if (roomPersistPort.findByRoomId(roomId) != null) {
            throw ErpException.of(ErrorCode.ROOM_DUPLICATE, Map.of("roomId", roomId));
        }

        try {
            RoomDomain saved = roomPersistPort.save(roomDomain);
            LOGGER.info("Created room {}", roomId);
            return saved;
        } catch (ErpException ex) {
            LOGGER.error("Business error while creating room {}: {}", roomId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating room {}", roomId, ex);
            throw ErpException.wrap(ErrorCode.ROOM_CREATE_FAILED, ex, Map.of("roomId", roomId));
        }
    }

    @Override
    public RoomDomain updateRoom(RoomDomain roomDomain) {
        String roomId = roomDomain.getRoomId();
        LOGGER.info("Updating room {}", roomId);

        if (roomPersistPort.findByRoomId(roomId) == null) {
            throw ErpException.of(ErrorCode.ROOM_NOT_FOUND, Map.of("roomId", roomId));
        }

        try {
            RoomDomain updated = roomPersistPort.update(roomDomain);
            LOGGER.info("Updated room {}", roomId);
            return updated;
        } catch (ErpException ex) {
            LOGGER.error("Business error while updating room {}: {}", roomId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating room {}", roomId, ex);
            throw ErpException.wrap(ErrorCode.ROOM_UPDATE_FAILED, ex, Map.of("roomId", roomId));
        }
    }

    @Override
    public boolean deleteRoom(String roomId) {
        LOGGER.info("Deleting room {}", roomId);

        if (roomPersistPort.findByRoomId(roomId) == null) {
            throw ErpException.of(ErrorCode.ROOM_NOT_FOUND, Map.of("roomId", roomId));
        }

        try {
            boolean deleted = roomPersistPort.deleteByRoomId(roomId);
            LOGGER.info("Deleted room {}: {}", roomId, deleted);
            return deleted;
        } catch (ErpException ex) {
            LOGGER.error("Business error while deleting room {}: {}", roomId, ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while deleting room {}", roomId, ex);
            throw ErpException.wrap(ErrorCode.ROOM_DELETE_FAILED, ex, Map.of("roomId", roomId));
        }
    }

    @Override
    public List<RoomDomain> listRooms() {
        LOGGER.info("Listing all rooms");
        try {
            return roomPersistPort.findAll();
        } catch (ErpException ex) {
            LOGGER.error("Business error while listing rooms: {}", ex.getErrorCode().getCode(), ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while listing rooms", ex);
            throw ErpException.wrap(ErrorCode.ROOM_FETCH_FAILED, ex, Map.of());
        }
    }
}
