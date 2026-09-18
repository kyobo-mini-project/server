package com.kyobo.server.mapper;

import java.util.List;

import com.kyobo.server.entity.Seat;

public interface SeatMapper {
    List<Seat> findActiveByRoomId(Integer roomId);

    List<Integer> findBookedSeatIdsByScreeningId(Integer screeningId);
}
