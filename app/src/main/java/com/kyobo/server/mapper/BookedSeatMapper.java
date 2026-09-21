package com.kyobo.server.mapper;

import com.kyobo.server.entity.BookedSeat;

public interface BookedSeatMapper {
    int upsert(BookedSeat bookedSeat);

    int deactivateByBooking(int bookingId);}
