package com.kyobo.server.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Env {
    private Env() {
    }

    public static Properties load() {
        Path path = Path.of(".env");
        if (!Files.exists(path)) {
            path = Path.of("..", ".env");
        }

        Properties props = new Properties();
        if (!Files.exists(path)) {
            return props;
        }

        try {
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
        } catch (IOException e) {
            throw new IllegalStateException(".env를 읽을 수 없습니다.", e);
        }
        return props;
    }

    public static String dbUrl() {
        return required(firstNonBlank(load().getProperty("DB_URL"), System.getenv("DB_URL")), "DB_URL");
    }

    public static String dbUsername() {
        return required(firstNonBlank(load().getProperty("DB_USERNAME"), System.getenv("DB_USERNAME")), "DB_USERNAME");
    }

    public static String dbPassword() {
        return required(firstNonBlank(load().getProperty("DB_PASSWORD"), System.getenv("DB_PASSWORD")), "DB_PASSWORD");
    }

    /** AES 암호화용 비밀키 (평문 문자열 → SHA-256으로 32바이트 키 유도) */
    public static String encryptionSecret() {
        return required(
                firstNonBlank(load().getProperty("ENCRYPTION_SECRET"), System.getenv("ENCRYPTION_SECRET")),
                "ENCRYPTION_SECRET");
    }

    public static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + "이(가) 없습니다. 프로젝트 루트 .env 또는 환경 변수를 확인하세요.");
        }
        return value;
    }
}
