package com.kyobo.server.entity;

// 날짜만 표현하는 타입
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
/** 상영회차 등록 등에 쓰는 영화 기본 정보. */
public class Movie {
    private Integer movieId;
    private String title;
    private Integer ageLimit;
    private Integer runningTime;
    private LocalDate releaseDate;
    private String content;
    private String director;
}