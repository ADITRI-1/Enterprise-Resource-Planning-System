package com.iiitd.erp.persistent.entity.erp_db;


import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rooms", schema = "erp_db")
public class Room {
    @Id
    @Column(name = "room_id", columnDefinition = "uuid")
    private UUID roomId;

    @Column(name = "room_number", nullable = false, unique = true, length = 20)
    private String roomNumber;

    @Column(nullable = false, length = 100)
    private String building;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "type", length = 40)
    private String type;

}