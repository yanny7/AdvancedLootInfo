package com.yanny.awi.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yanny.aci.test.utils.TestUtils;
import com.yanny.awi.Utils;
import com.yanny.awi.configuration.AwiConfig;
import com.yanny.awi.configuration.ConfigUtils;
import com.yanny.awi.platform.TestPlatformHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {
    @TempDir
    Path configDir;

    @BeforeEach
    public void setUp() {
        TestPlatformHelper.CONFIG_DIR = configDir;
    }

    @AfterEach
    public void tearDown() {
        TestPlatformHelper.CONFIG_DIR = null;
    }

    @Test
    public void testCreatedConfigFile() throws IOException {
        AwiConfig config = ConfigUtils.readConfiguration();

        assertTrue(Files.exists(configFile()));
        assertEquals(expectedConfig(), writtenConfig());
        assertEquals(AwiConfig.CURRENT_VERSION, config.configVersion);
    }

    @Test
    public void testMissingKeysUseDefaultValues() throws IOException {
        writeConfig("{\"configVersion\": " + AwiConfig.CURRENT_VERSION + "}");

        AwiConfig config = ConfigUtils.readConfiguration();

        assertTrue(backups().isEmpty());
        assertFalse(config.logMoreStatistics);
        assertTrue(config.showInGameNames);
    }

    @Test
    public void testCustomValuesAreLoaded() {
        writeConfig("""
                {
                  "configVersion": %d,
                  "logMoreStatistics": true,
                  "showInGameNames": false
                }
                """.formatted(AwiConfig.CURRENT_VERSION));

        AwiConfig config = ConfigUtils.readConfiguration();

        assertTrue(config.logMoreStatistics);
        assertFalse(config.showInGameNames);
    }

    @Test
    public void testOutdatedConfigIsRecreated() throws IOException {
        writeConfig("{\"configVersion\": 0, \"showInGameNames\": false}");

        AwiConfig config = ConfigUtils.readConfiguration();

        assertEquals(List.of("{\"configVersion\": 0, \"showInGameNames\": false}"), backups());
        assertEquals(expectedConfig(), writtenConfig());
        assertEquals(AwiConfig.CURRENT_VERSION, config.configVersion);
        assertTrue(config.showInGameNames);
    }

    private Path configFile() {
        return configDir.resolve(Utils.MOD_ID).resolve(Utils.COMMON_CONFIG_NAME);
    }

    private List<String> backups() throws IOException {
        return TestUtils.readBackups(configDir.resolve(Utils.MOD_ID), Utils.COMMON_CONFIG_NAME);
    }

    private void writeConfig(String content) {
        try {
            Files.createDirectories(configFile().getParent());
            Files.writeString(configFile(), content);
        } catch (IOException e) {
            throw new AssertionError("Failed to prepare configuration file", e);
        }
    }

    private JsonElement writtenConfig() throws IOException {
        return JsonParser.parseString(Files.readString(configFile()));
    }

    private static JsonElement expectedConfig() throws IOException {
        try (InputStream stream = ConfigTest.class.getResourceAsStream("/config/" + Utils.COMMON_CONFIG_NAME)) {
            assertNotNull(stream, "Missing expected configuration file");
            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
