package com.kyobo.server.mapper;

import com.kyobo.server.entity.Admin;

public interface AdminMapper {
    Admin findByCode(String code);
}
