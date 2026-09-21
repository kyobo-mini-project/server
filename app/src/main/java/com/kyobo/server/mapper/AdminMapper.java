package com.kyobo.server.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.kyobo.server.entity.Admin;
import com.kyobo.server.entity.RoomAdmin;

public interface AdminMapper {
    Admin findByCode(String code);

    List<RoomAdmin> findRoomsByCinemaId(@Param("cinemaId") int cinemaId);

    void lockBookings();

    RoomAdmin findRoomForUpdate(
            @Param("cinemaId") int cinemaId,
            @Param("roomId") int roomId);

    List<RoomAdmin> findBookedBuyers(
            @Param("cinemaId") int cinemaId,
            @Param("roomId") int roomId);

    int updateRoomActive(
            @Param("cinemaId") int cinemaId,
            @Param("roomId") int roomId,
            @Param("active") boolean active);

    List<RoomAdmin> findSeatsByBookingIds(
            @Param("bookingIds") List<Integer> bookingId);
}
