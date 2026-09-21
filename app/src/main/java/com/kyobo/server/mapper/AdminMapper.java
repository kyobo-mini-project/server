package com.kyobo.server.mapper;

import com.kyobo.server.entity.Admin;

public interface AdminMapper {
    Admin findByCode(String code);

    List<Room> findRoomsByCinemaId(@Param("cinemaId") int cinemaId);

    void lockBookings();

    Room findRoomForUpdate(
            @Param("cinemaId") int cinemaId,
            @Param("roomId") int roomId);

    List<Room> findBookedBuyers(
            @Param("cinemaId") int cinemaId,
            @Param("roomId") int roomId);

    int updateRoomActive(
            @Param("cinemaId") int cinemaId,
            @Param("roomId") int roomId,
            @Param("active") boolean active);
}
