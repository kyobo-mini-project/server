package com.kyobo.server.entity;

public class Booking {
    private Integer bookingId;
    private Integer userId;
    private Integer movieId;
    private Integer screeningId;
    private String movieTitle;
    private java.time.LocalDate screeningDate;
    private java.time.LocalTime startTime;
    private String cinemaName;
    private String roomName;
    private String seatCodes;
    private String bookingStatus;
    private java.time.LocalDateTime bookingDate;
    private Integer floor;

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

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Integer getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Integer screeningId) {
        this.screeningId = screeningId;
    }
    public String getMovieTitle() {
        return movieTitle;
    }

    public java.time.LocalDate getScreeningDate() {
        return screeningDate;
    }

    public java.time.LocalTime getStartTime() {
        return startTime;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getSeatCodes() {
        return seatCodes;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public java.time.LocalDateTime getBookingDate() {
        return bookingDate;
    }
    
    public Integer getFloor() {
        return floor;
    }
}
