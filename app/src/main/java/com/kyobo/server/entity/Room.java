package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Room {
    private Integer roomId;
    private Integer cinemaId;
    private String roomName;
    private Integer floor;
    private Boolean isActive;
}
