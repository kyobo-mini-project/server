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
        Properties mybatisProps = new Properties();
        mybatisProps.setProperty("db.url", Env.dbUrl());
        mybatisProps.setProperty("db.username", Env.dbUsername());
        mybatisProps.setProperty("db.password", Env.dbPassword());

        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            return new SqlSessionFactoryBuilder().build(in, mybatisProps);
        } catch (IOException e) {
            throw new IllegalStateException("MyBatis 설정을 읽을 수 없습니다.", e);
        }
    }
}
