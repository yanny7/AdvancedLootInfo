package com.yanny.aci.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.configuration.CoreConfigUtils;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CoreConfigUtilsTest {
    private static final String MOD_ID = "aci_test";
    private static final String FILE_NAME = "aci_test_common.json";

    @TempDir
    Path configDir;

    @Test
    public void testCreatedConfigFile() throws IOException {
        TestConfig config = read();

        assertTrue(Files.exists(configFile()));
        assertEquals(TestConfig.CURRENT_VERSION, config.configVersion);
        assertFalse(Files.exists(backupFile()));

        JsonObject written = writtenConfig();

        assertEquals(TestConfig.CURRENT_VERSION, written.get("configVersion").getAsInt());
        assertEquals("minecraft:stone", written.get("location").getAsString());
    }

    @Test
    public void testExistingConfigFileIsNotRewritten() throws IOException {
        read();

        String created = Files.readString(configFile());

        read();

        assertEquals(created, Files.readString(configFile()));
        assertFalse(Files.exists(backupFile()));
    }

    @Test
    public void testCustomValuesAreLoaded() {
        writeConfig("""
                {
                  "configVersion": %d,
                  "flag": false,
                  "location": "minecraft:dirt",
                  "values": ["only"]
                }
                """.formatted(TestConfig.CURRENT_VERSION));

        TestConfig config = read();

        assertFalse(config.flag);
        assertEquals(Identifier.fromNamespaceAndPath("minecraft", "dirt"), config.location);
        assertEquals(List.of("only"), config.values);
    }

    @Test
    public void testMissingFieldsFallBackToDefaults() {
        writeConfig("{\"configVersion\": %d}".formatted(TestConfig.CURRENT_VERSION));

        TestConfig config = read();

        assertTrue(config.flag);
        assertEquals(new TestConfig().location, config.location);
        assertEquals(new TestConfig().values, config.values);
    }

    @Test
    public void testOutdatedConfigIsRecreated() throws IOException {
        String outdated = "{\"configVersion\": 0, \"flag\": false}";

        writeConfig(outdated);

        TestConfig config = read();

        assertTrue(Files.exists(backupFile()));
        assertEquals(outdated, Files.readString(backupFile()));
        assertEquals(TestConfig.CURRENT_VERSION, config.configVersion);
        assertTrue(config.flag);
    }

    @Test
    public void testExistingBackupIsReplaced() throws IOException {
        writeConfig("{\"configVersion\": 0}");
        Files.writeString(backupFile(), "previous backup");

        read();

        assertEquals("{\"configVersion\": 0}", Files.readString(backupFile()));
    }

    @Test
    public void testEmptyConfigIsRecreated() throws IOException {
        writeConfig("");

        TestConfig config = read();

        assertEquals(TestConfig.CURRENT_VERSION, config.configVersion);
        assertEquals(TestConfig.CURRENT_VERSION, writtenConfig().get("configVersion").getAsInt());
    }

    @Test
    public void testMalformedConfigIsRecreated() throws IOException {
        String malformed = "{ this is not json";

        writeConfig(malformed);

        TestConfig config = read();

        assertTrue(Files.exists(backupFile()));
        assertEquals(malformed, Files.readString(backupFile()));
        assertEquals(TestConfig.CURRENT_VERSION, config.configVersion);
        assertEquals(TestConfig.CURRENT_VERSION, writtenConfig().get("configVersion").getAsInt());
    }

    @Test
    public void testMissingConfigDirReturnsDefaults() {
        TestConfig config = CoreConfigUtils.readConfiguration(null, MOD_ID, FILE_NAME, TestConfig.CODEC, JsonOps.INSTANCE, TestConfig::new);

        assertEquals(0, config.configVersion);
        assertEquals(new TestConfig().values, config.values);
    }

    @Test
    public void testUncreatableConfigDirReturnsDefaults() throws IOException {
        // a file where the mod's config directory should go - createDirectories cannot succeed
        Files.writeString(configDir.resolve(MOD_ID), "not a directory");

        TestConfig config = read();

        assertEquals(0, config.configVersion);
        assertEquals(new TestConfig().values, config.values);
    }

    private TestConfig read() {
        return CoreConfigUtils.readConfiguration(configDir, MOD_ID, FILE_NAME, TestConfig.CODEC, JsonOps.INSTANCE, TestConfig::new);
    }

    private Path configFile() {
        return configDir.resolve(MOD_ID).resolve(FILE_NAME);
    }

    private Path backupFile() {
        return configDir.resolve(MOD_ID).resolve(FILE_NAME + ".bak");
    }

    private void writeConfig(String content) {
        try {
            Files.createDirectories(configFile().getParent());
            Files.writeString(configFile(), content);
        } catch (IOException e) {
            throw new AssertionError("Failed to prepare configuration file", e);
        }
    }

    private JsonObject writtenConfig() throws IOException {
        return JsonParser.parseString(Files.readString(configFile())).getAsJsonObject();
    }
}
