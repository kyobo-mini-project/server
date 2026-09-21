package com.kyobo.server.entity;

/** 영화별 점유율 계산 결과. */
public class MovieOccupancy {
    private int movieId;
    private long soldSeatCount;
    private long availableSeatCount;
    private double occupancyRate;

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public long getSoldSeatCount() {
        return soldSeatCount;
    }

    public void setSoldSeatCount(long soldSeatCount) {
        this.soldSeatCount = soldSeatCount;
    }

    public long getAvailableSeatCount() {
        return availableSeatCount;
    }

    public void setAvailableSeatCount(long availableSeatCount) {
        this.availableSeatCount = availableSeatCount;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }
}
