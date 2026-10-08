package com.yanny.aci.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.configuration.CoreConfigUtils;
import com.yanny.aci.test.utils.TestUtils;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(backups().isEmpty());

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
        assertTrue(backups().isEmpty());
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
        assertEquals(ResourceLocation.fromNamespaceAndPath("minecraft", "dirt"), config.location);
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

        assertEquals(List.of(outdated), backups());
        assertEquals(TestConfig.CURRENT_VERSION, config.configVersion);
        assertTrue(config.flag);
    }

    @Test
    public void testExistingBackupsAreKept() throws IOException {
        writeConfig("{\"configVersion\": 0}");
        read();
        writeConfig("{\"configVersion\": 0, \"flag\": false}");
        read();

        assertEquals(List.of("{\"configVersion\": 0}", "{\"configVersion\": 0, \"flag\": false}"), backups());
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

        assertEquals(List.of(malformed), backups());
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

    private List<String> backups() throws IOException {
        return TestUtils.readBackups(configDir.resolve(MOD_ID), FILE_NAME);
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
