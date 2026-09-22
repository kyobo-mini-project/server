package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** 영화별 점유율 계산 결과. */
public class MovieOccupancy {
    private int movieId;
    private long soldSeatCount;
    private long availableSeatCount;
    private double occupancyRate;
}
