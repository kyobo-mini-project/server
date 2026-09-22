package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookedSeat {
    private Integer bookedSeatId;
    private Integer bookingId;
    private Integer seatId;
    private Integer screeningId;
    private Boolean isActive;
}
