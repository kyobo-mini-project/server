package com.kyobo.server.mapper;

import java.util.List;

import com.kyobo.server.entity.Room;

public interface RoomMapper {
    List<Room> findActiveByCinemaId(int cinemaId);
}
