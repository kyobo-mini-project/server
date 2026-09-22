package com.kyobo.server.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/** 영화 목록 화면에 표시할 영화와 결합된 장르 정보. */
public class MovieListItem {
    private int movieId;
    private String title;
    private String genres;
    private double occupancyRate;
}
