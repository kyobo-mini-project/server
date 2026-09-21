package com.kyobo.server.entity;

public class RoomAdmin {
    private Integer roomId;
    private String roomName;
    private Integer floor;
    private Boolean active;

    private String loginId;
    private String phoneNumber;
    private Long seatCount;
    private String seatLocations;

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;}

    public Long getSeatCount() { return seatCount; }
    public void setSeatCount(Long seatCount) { this.seatCount = seatCount; }

    public String getSeatLocations() { return seatLocations; }
    public void setSeatLocations(String seatLocations) {
        this.seatLocations = seatLocations;}
}