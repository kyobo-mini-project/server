package com.kyobo.server.entity;

/** 영화별 좌석 수 집계 결과. */
public class MovieSeatCount {
    private int movieId;
    private long seatCount;

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public long getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(long seatCount) {
        this.seatCount = seatCount;
    }
}
