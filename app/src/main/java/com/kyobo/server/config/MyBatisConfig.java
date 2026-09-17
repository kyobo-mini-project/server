package com.kyobo.server.config;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class MyBatisConfig {
    private static final SqlSessionFactory FACTORY = build();

    private MyBatisConfig() {
    }

    public static SqlSessionFactory sqlSessionFactory() {
        return FACTORY;
    }

    private static SqlSessionFactory build() {
        Properties env = Env.load();
        Properties mybatisProps = new Properties();
        mybatisProps.setProperty("db.url", required(Env.firstNonBlank(env.getProperty("DB_URL"), System.getenv("DB_URL")), "DB_URL"));
        mybatisProps.setProperty("db.username", required(Env.firstNonBlank(env.getProperty("DB_USERNAME"), System.getenv("DB_USERNAME")), "DB_USERNAME"));
        mybatisProps.setProperty("db.password", required(Env.firstNonBlank(env.getProperty("DB_PASSWORD"), System.getenv("DB_PASSWORD")), "DB_PASSWORD"));

        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            return new SqlSessionFactoryBuilder().build(in, mybatisProps);
        } catch (IOException e) {
            throw new IllegalStateException("MyBatis 설정을 읽을 수 없습니다.", e);
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + "이(가) 없습니다. 프로젝트 루트 .env 또는 환경 변수를 확인하세요.");
        }
        return value;
    }
}
