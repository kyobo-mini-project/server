package com.kyobo.server.mapper;

public interface BookingMapper {
    int findActiveBookingsByUserId(Integer userId);
}
