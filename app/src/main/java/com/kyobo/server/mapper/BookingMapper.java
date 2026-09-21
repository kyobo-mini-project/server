package com.kyobo.server.mapper;
import com.kyobo.server.entity.Booking;

public interface BookingMapper {
    int findActiveBookingsByUserId(Integer userId);

    int insert(Booking booking);
}
