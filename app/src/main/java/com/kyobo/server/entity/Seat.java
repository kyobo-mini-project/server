package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Seat {
    private Integer seatId;
    private Integer roomId;
    private String rowName;
    private Integer colNum;
    private Boolean active;
    /** 좌석 배치도 조회(findSeatMap) 시에만 채워짐 */
    private Boolean isBooked;

    /** 좌석 코드 표시용 (ex. 열 C, 행 3 -> "C3") */
    public String seatCode() {
        return rowName + colNum;
    }

    public boolean booked() {
        return Boolean.TRUE.equals(isBooked);
    }
}
