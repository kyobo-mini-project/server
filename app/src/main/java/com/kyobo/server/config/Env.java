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

    public static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
