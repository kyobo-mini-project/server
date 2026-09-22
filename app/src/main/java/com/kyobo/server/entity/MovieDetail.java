package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class MovieDetail {
    private Integer movieId;
    private String title;
    private String genres;
    private String director;
    private Integer runningTime;
    private Timestamp releaseDate;
    private Integer ageLimit;
    private String content;
    private double occupancyRate;
    private int occupancyRank;
}