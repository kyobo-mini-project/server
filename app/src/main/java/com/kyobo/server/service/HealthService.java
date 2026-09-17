package com.kyobo.server.service;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.mapper.HealthMapper;

public class HealthService {
    public int ping() {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            return session.getMapper(HealthMapper.class).ping();
        }
    }
}
