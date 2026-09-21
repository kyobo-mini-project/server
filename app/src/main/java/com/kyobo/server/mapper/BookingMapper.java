package com.kyobo.server.mapper;

import com.kyobo.server.entity.Booking;

public interface BookingMapper {
    int insert(Booking booking);

    int findActiveBookingsByUserId(Integer userId);
}
