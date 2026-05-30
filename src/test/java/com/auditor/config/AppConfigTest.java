package com.auditor.config;

import org.junit.jupiter.api.Test;

import tools.jackson.core.exc.JacksonIOException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void loadConfig() {
        assertThrows(JacksonIOException.class, () -> {
            AppConfig.loadConfig(Path.of("configuration.json"));
        });
    }
}