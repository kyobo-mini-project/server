package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomAdmin {
    private Integer roomId;
    private String roomName;
    private Integer floor;
    private Boolean active;

    private Integer bookingId;
    private Integer userId;
    private String loginId;
    private String phoneNumber;

    private Integer bookedSeatId;
    private Integer screeningId;
    private String rowName;
    private Integer colNum;
    private String movieTitle;
    private java.time.LocalDate screeningDate;
    private java.time.LocalTime startTime;
}
