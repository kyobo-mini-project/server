package com.kyobo.server.entity;

/** 상영회차 등록 등에 쓰는 영화 기본 정보. */
public class Movie {
    private Integer movieId;
    private String title;
    private Integer runningTime;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(Integer runningTime) {
        this.runningTime = runningTime;
    }
}
