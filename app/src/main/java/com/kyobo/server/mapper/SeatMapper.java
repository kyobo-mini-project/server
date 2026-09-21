package com.kyobo.server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kyobo.server.entity.Seat;
import com.kyobo.server.entity.RoomAdmin;

public interface SeatMapper {
    List<Seat> findSeatMap(@Param("roomId") int roomId, @Param("screeningId") int screeningId);

    List<Seat> findByRoom(@Param("cinemaId") int cinemaId, @Param("roomId") int roomId);

    Seat findForUpdate(@Param("cinemaId") int cinemaId,
                       @Param("roomId") int roomId, @Param("seatId") int seatId);

    List<RoomAdmin> findUpcomingBookings(@Param("seatId") int seatId);

    int updateActive(@Param("seatId") int seatId, @Param("active") boolean active);

    List<Seat> findBookableForUpdate(@Param("screeningId") int screeningId,
                                    @Param("seatIds") List<Integer> seatIds);
}
