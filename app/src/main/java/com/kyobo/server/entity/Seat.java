package com.kyobo.server.entity;

public class Seat {
    private Integer seatId;
    private Integer roomId;
    private String rowName;
    private Integer colNum;
    private Boolean active;
    /** 좌석 배치도 조회(findSeatMap) 시에만 채워짐 */
    private Boolean isBooked;

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRowName() {
        return rowName;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public Integer getColNum() {
        return colNum;
    }

    public void setColNum(Integer colNum) {
        this.colNum = colNum;
    }

    public Boolean getIsBooked() {
        return isBooked;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setIsBooked(Boolean isBooked) {
        this.isBooked = isBooked;
    }

    /** 좌석 코드 표시용 (ex. 열 C, 행 3 -> "C3") */
    public String seatCode() {
        return rowName + colNum;
    }

    public boolean booked() {
        return Boolean.TRUE.equals(isBooked);
    }
}
