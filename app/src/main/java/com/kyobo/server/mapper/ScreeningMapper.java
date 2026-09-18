package com.kyobo.server.mapper;

import java.util.List;

import com.kyobo.server.entity.Screening;

public interface ScreeningMapper {
    List<Screening> findByMovieId(int movieId);
}
