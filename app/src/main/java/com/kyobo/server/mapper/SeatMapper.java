package com.kyobo.server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kyobo.server.entity.Seat;

public interface SeatMapper {
    List<Seat> findSeatMap(@Param("roomId") int roomId, @Param("screeningId") int screeningId);
}
