package com.kyobo.server.mapper;
import com.kyobo.server.entity.Booking;

public interface BookingMapper {
    int findActiveBookingsByUserId(Integer userId);

public interface BookingMapper {
    int insert(Booking booking);
}
