package com.kyobo.server;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.repository.HealthMapper;
import org.apache.ibatis.session.SqlSession;

public class App {
    public static void main(String[] args) {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            HealthMapper mapper = session.getMapper(HealthMapper.class);
            System.out.println("MyBatis 연결 성공: " + mapper.ping());
        }
    }
}
