package com.kyobo.server.entity;

/** 영화 목록 화면에 표시할 영화와 결합된 장르 정보. */
public class MovieListItem {
    private int movieId;
    private String title;
    private String genres;

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }
}
