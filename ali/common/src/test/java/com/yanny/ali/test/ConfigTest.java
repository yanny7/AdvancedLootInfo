package com.yanny.ali.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yanny.ali.Utils;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.configuration.ConfigUtils;
import com.yanny.ali.platform.TestPlatformHelper;
import net.minecraft.resources.ResourceLocation;
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
        AliConfig config = ConfigUtils.readConfiguration();

        assertTrue(Files.exists(configFile()));
        assertEquals(expectedConfig(), writtenConfig());
        assertEquals(AliConfig.CURRENT_VERSION, config.configVersion);
    }

    @Test
    public void testExistingConfigFileIsNotRewritten() throws IOException {
        ConfigUtils.readConfiguration();

        String created = Files.readString(configFile());

        ConfigUtils.readConfiguration();

        assertEquals(created, Files.readString(configFile()));
        assertFalse(Files.exists(backupFile()));
    }

    @Test
    public void testMissingKeysUseDefaultValues() throws IOException {
        writeConfig("{\"configVersion\": " + AliConfig.CURRENT_VERSION + "}");

        AliConfig config = ConfigUtils.readConfiguration();
        AliConfig defaults = new AliConfig();

        assertFalse(Files.exists(backupFile()));
        assertTrue(config.hideDefaultBlockLoot);
        assertTrue(config.showInGameNames);
        assertFalse(config.logMoreStatistics);
        assertEquals(defaults.blockCategories, config.blockCategories);
        assertEquals(defaults.entityCategories, config.entityCategories);
        assertEquals(defaults.gameplayCategories, config.gameplayCategories);
        assertEquals(defaults.tradeCategories, config.tradeCategories);
        assertEquals(defaults.disabledEntities, config.disabledEntities);
        assertEquals(defaults.defaultBlockLootConditions, config.defaultBlockLootConditions);
        assertEquals(defaults.defaultBlockLootFunctions, config.defaultBlockLootFunctions);
    }

    @Test
    public void testNullListsUseDefaultValues() {
        writeConfig("""
                {
                  "configVersion": %d,
                  "blockCategories": null,
                  "entityCategories": null,
                  "gameplayCategories": null,
                  "tradeCategories": null,
                  "disabledEntities": null,
                  "defaultBlockLootConditions": null,
                  "defaultBlockLootFunctions": null
                }
                """.formatted(AliConfig.CURRENT_VERSION));

        AliConfig config = ConfigUtils.readConfiguration();
        AliConfig defaults = new AliConfig();

        assertEquals(defaults.blockCategories, config.blockCategories);
        assertEquals(defaults.entityCategories, config.entityCategories);
        assertEquals(defaults.gameplayCategories, config.gameplayCategories);
        assertEquals(defaults.tradeCategories, config.tradeCategories);
        assertEquals(defaults.disabledEntities, config.disabledEntities);
        assertEquals(defaults.defaultBlockLootConditions, config.defaultBlockLootConditions);
        assertEquals(defaults.defaultBlockLootFunctions, config.defaultBlockLootFunctions);
    }

    @Test
    public void testCustomValuesAreLoaded() {
        writeConfig("""
                {
                  "configVersion": %d,
                  "hideDefaultBlockLoot": false,
                  "logMoreStatistics": true,
                  "disabledEntities": ["minecraft:sheep"],
                  "defaultBlockLootConditions": ["minecraft:survives_explosion", "minecraft:match_tool"],
                  "defaultBlockLootFunctions": []
                }
                """.formatted(AliConfig.CURRENT_VERSION));

        AliConfig config = ConfigUtils.readConfiguration();

        assertFalse(config.hideDefaultBlockLoot);
        assertTrue(config.logMoreStatistics);
        assertEquals(List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "sheep")), config.disabledEntities);
        assertEquals(List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "survives_explosion"), ResourceLocation.fromNamespaceAndPath("minecraft", "match_tool")),
                config.defaultBlockLootConditions);
        assertEquals(List.of(), config.defaultBlockLootFunctions);
    }

    @Test
    public void testOutdatedConfigIsRecreated() throws IOException {
        writeConfig("{\"configVersion\": 0, \"logMoreStatistics\": true}");

        AliConfig config = ConfigUtils.readConfiguration();

        assertTrue(Files.exists(backupFile()));
        assertEquals("{\"configVersion\": 0, \"logMoreStatistics\": true}", Files.readString(backupFile()));
        assertEquals(expectedConfig(), writtenConfig());
        assertEquals(AliConfig.CURRENT_VERSION, config.configVersion);
        assertFalse(config.logMoreStatistics);
    }

    @Test
    public void testEmptyConfigIsRecreated() throws IOException {
        writeConfig("");

        AliConfig config = ConfigUtils.readConfiguration();

        assertEquals(expectedConfig(), writtenConfig());
        assertEquals(AliConfig.CURRENT_VERSION, config.configVersion);
    }

    private Path configFile() {
        return configDir.resolve(Utils.MOD_ID).resolve(Utils.COMMON_CONFIG_NAME);
    }

    private Path backupFile() {
        return configDir.resolve(Utils.MOD_ID).resolve(Utils.COMMON_CONFIG_NAME + ".bak");
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
