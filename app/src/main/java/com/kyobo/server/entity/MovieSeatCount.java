package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** 영화별 좌석 수 집계 결과. */
public class MovieSeatCount {
    private int movieId;
    private long seatCount;
}
