package com.kyobo.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class App {
    public static void main(String[] args) throws Exception {
        Properties env = loadEnv();
        String url = firstNonBlank(env.getProperty("DB_URL"), System.getenv("DB_URL"));
        String user = firstNonBlank(env.getProperty("DB_USERNAME"), System.getenv("DB_USERNAME"));
        String password = firstNonBlank(env.getProperty("DB_PASSWORD"), System.getenv("DB_PASSWORD"));

        if (url == null || url.isBlank()) {
            throw new IllegalStateException("DB_URL이 없습니다. 프로젝트 루트 .env 또는 환경 변수를 확인하세요.");
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, value FROM playing_with_neon")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                float value = rs.getFloat("value");
                System.out.printf("id=%d, name=%s, value=%s%n", id, name, value);
            }
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static Properties loadEnv() throws IOException {
        Path path = Path.of(".env");
        if (!Files.exists(path)) {
            path = Path.of("..", ".env");
        }

        Properties props = new Properties();
        if (!Files.exists(path)) {
            return props;
        }

        for (String line : Files.readAllLines(path)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int idx = trimmed.indexOf('=');
            if (idx < 0) {
                continue;
            }
            props.setProperty(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
        }
        return props;
    }
}
