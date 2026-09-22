package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class Screening {
    private Integer screeningId;
    private Integer movieId;
    private Integer cinemaId;
    private String cinemaName;
    private Integer roomId;
    private String roomName;
    private Integer floor;
    private LocalDate screeningDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String movieTitle;
}
