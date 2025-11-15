package com.iiitd.erp.persistent.persistence;

import com.iiitd.erp.Domain.RoomDomain;
import com.iiitd.erp.core.RoomPersistPort;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.persistent.entity.erp_db.Room;
import com.iiitd.erp.persistent.persistence.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iiitd.erp.mappers.DomainToEntityMapper.DOMAIN_TO_ENTITY_MAPPER_INSTANCE;
import static com.iiitd.erp.mappers.EntityToDomainMapper.ENTITY_TO_DOMAIN_MAPPER_INSTANCE;

@Repository
@Transactional
public class RoomPersistPortImpl implements RoomPersistPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomPersistPortImpl.class);

    private final RoomRepository roomRepository;

    public RoomPersistPortImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomDomain findByRoomNumber(String roomNumber) {
        LOGGER.debug("Fetching room by number {}", roomNumber);
        try {
            return roomRepository.findByRoomNumber(roomNumber)
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::roomToRoomDomain)
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch room {}", roomNumber, ex);
            throw ErpException.wrap(ErrorCode.ROOM_FETCH_FAILED, ex, Map.of("roomNumber", roomNumber));
        }
    }

    @Override
    public RoomDomain findByRoomId(String roomId) {
        LOGGER.debug("Fetching room by id {}", roomId);
        try {
            return roomRepository.findById(roomId)
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::roomToRoomDomain)
                    .orElse(null);
        } catch (IllegalArgumentException ex) {
            throw ErpException.of(ErrorCode.ROOM_NOT_FOUND, Map.of("roomId", roomId));
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch room {}", roomId, ex);
            throw ErpException.wrap(ErrorCode.ROOM_FETCH_FAILED, ex, Map.of("roomId", roomId));
        }
    }

    @Override
    public RoomDomain save(RoomDomain roomDomain) {
        LOGGER.debug("Persisting room {}", roomDomain.getRoomNumber());
        try {
            Room entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.roomDomainToRoom(roomDomain);
            Room saved = roomRepository.save(entity);
            LOGGER.info("Persisted room {}", roomDomain.getRoomNumber());
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.roomToRoomDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist room {}", roomDomain.getRoomNumber(), ex);
            throw ErpException.wrap(ErrorCode.ROOM_CREATE_FAILED, ex, Map.of("roomNumber", roomDomain.getRoomNumber()));
        }
    }

    @Override
    public RoomDomain update(RoomDomain roomDomain) {
        String roomId = roomDomain.getRoomId();
        Room existing = safeFetch(roomId);
        try {
            Room entity = DOMAIN_TO_ENTITY_MAPPER_INSTANCE.roomDomainToRoom(roomDomain);
            entity.setRoomId(existing.getRoomId());
            Room saved = roomRepository.save(entity);
            LOGGER.info("Updated room {}", roomId);
            return ENTITY_TO_DOMAIN_MAPPER_INSTANCE.roomToRoomDomain(saved);
        } catch (Exception ex) {
            LOGGER.error("Failed to update room {}", roomId, ex);
            throw ErpException.wrap(ErrorCode.ROOM_UPDATE_FAILED, ex, Map.of("roomId", roomId));
        }
    }

    @Override
    public boolean deleteByRoomId(String roomId) {
        Room existing = safeFetch(roomId);
        try {
            roomRepository.delete(existing);
            LOGGER.info("Deleted room {}", roomId);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to delete room {}", roomId, ex);
            throw ErpException.wrap(ErrorCode.ROOM_DELETE_FAILED, ex, Map.of("roomId", roomId));
        }
    }

    @Override
    public List<RoomDomain> findAll() {
        LOGGER.debug("Fetching all rooms");
        try {
            return roomRepository.findAll().stream()
                    .map(ENTITY_TO_DOMAIN_MAPPER_INSTANCE::roomToRoomDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch rooms", ex);
            throw ErpException.wrap(ErrorCode.ROOM_FETCH_FAILED, ex, Map.of());
        }
    }

    private Room safeFetch(String roomId) {
        try {
            return roomRepository.findById(roomId)
                    .orElseThrow(() -> ErpException.of(ErrorCode.ROOM_NOT_FOUND, Map.of("roomId", roomId)));
        } catch (IllegalArgumentException ex) {
            throw ErpException.of(ErrorCode.ROOM_NOT_FOUND, Map.of("roomId", roomId));
        }
    }
}
