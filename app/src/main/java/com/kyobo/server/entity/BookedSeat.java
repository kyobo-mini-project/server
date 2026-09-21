package com.kyobo.server.entity;

public class BookedSeat {
    private Integer bookedSeatId;
    private Integer bookingId;
    private Integer seatId;
    private Integer screeningId;

    public Integer getBookedSeatId() {
        return bookedSeatId;
    }

    public void setBookedSeatId(Integer bookedSeatId) {
        this.bookedSeatId = bookedSeatId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public Integer getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Integer screeningId) {
        this.screeningId = screeningId;
    }
}
