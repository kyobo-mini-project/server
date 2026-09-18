package com.kyobo.server.entity;

public class Booking {
    private Integer bookingId;
    private Integer userId;
    /** bookings.movie_id 컬럼이 VARCHAR라서 movies.movie_id(INTEGER)를 문자열로 변환해 저장한다. */
    private String movieId;
    private Integer screeningId;

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public Integer getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Integer screeningId) {
        this.screeningId = screeningId;
    }
}
