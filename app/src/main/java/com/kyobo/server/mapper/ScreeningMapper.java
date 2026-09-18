package com.kyobo.server.mapper;

import com.kyobo.server.entity.Screening;

public interface ScreeningMapper {
    Screening findDetailByScreeningId(Integer screeningId);
}
