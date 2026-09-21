package com.kyobo.server.entity;

public class RoomAdmin {

    private Integer roomId;
    private String roomName;
    private Integer floor;
    private Boolean active;

    private Integer bookingId;
    private Integer userId;
    private String loginId;
    private String phoneNumber;

    private Integer bookedSeatId;
    private Integer screeningId;
    private String rowName;
    private Integer colNum;

    public Integer getRoomId() {
        return roomId;}

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;}

    public String getRoomName() {
        return roomName;}

    public void setRoomName(String roomName) {
        this.roomName = roomName;}

    public Integer getFloor() {
        return floor;}

    public void setFloor(Integer floor) {
        this.floor = floor;}

    public Boolean getActive() {
        return active;}

    public void setActive(Boolean active) {
        this.active = active;}

    public Integer getBookingId() {
        return bookingId;}

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;}

    public Integer getUserId() {
        return userId;}

    public void setUserId(Integer userId) {
        this.userId = userId;}

    public String getLoginId() {
        return loginId;}

    public void setLoginId(String loginId) {
        this.loginId = loginId;}

    public String getPhoneNumber() {
        return phoneNumber;}

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;}

    public Integer getBookedSeatId() {
        return bookedSeatId;}

    public void setBookedSeatId(Integer bookedSeatId) {
        this.bookedSeatId = bookedSeatId;}

    public Integer getScreeningId() {
        return screeningId;}

    public void setScreeningId(Integer screeningId) {
        this.screeningId = screeningId;}

    public String getRowName() {
        return rowName;}

    public void setRowName(String rowName) {
        this.rowName = rowName;}

    public Integer getColNum() {
        return colNum;}

    public void setColNum(Integer colNum) {
        this.colNum = colNum;}
}