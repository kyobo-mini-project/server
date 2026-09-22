package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Booking {
    private Integer bookingId;
    private Integer userId;
    private Integer movieId;
    private Integer screeningId;
    private String movieTitle;
    private java.time.LocalDate screeningDate;
    private java.time.LocalTime startTime;
    private String cinemaName;
    private String roomName;
    private String seatCodes;
    private Integer floor;
}
