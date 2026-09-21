package com.kyobo.server.entity;

// 날짜만 표현하는 타입
import java.time.LocalDate;

public class Movie {

    private Integer movieId;
    private String title;
    private Integer ageLimit;
    private Integer runningTime;
    private LocalDate releaseDate;
    private String content;
    private String director;

    // 영화 번호
    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    // 영화 제목
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // 관람등급
    public Integer getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(Integer ageLimit) {
        this.ageLimit = ageLimit;
    }

    // 러닝타임: 분 단위
    public Integer getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(Integer runningTime) {
        this.runningTime = runningTime;
    }

    // 개봉일
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    // 영화 소개
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // 감독
    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }


}