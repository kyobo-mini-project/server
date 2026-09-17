package com.kyobo.server;

import java.util.Scanner;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.controller.ConsoleController;
import com.kyobo.server.repository.HealthMapper;

public class App {
    public static void main(String[] args) {
        mybatisHealthCheck();

        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleController(scanner).run();
        }
    }

    private static void mybatisHealthCheck() {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            HealthMapper mapper = session.getMapper(HealthMapper.class);
            System.out.println("MyBatis 연결 성공: " + mapper.ping());
        }
    }
}
