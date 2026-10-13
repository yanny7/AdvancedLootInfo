package com.yanny.alicompat.gametest;

import com.yanny.aci.gametest.GoldenFiles;
import com.yanny.aci.gametest.GoldenLanguage;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.gametest.LootDataCapture;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

public class CompatDataGolden {
    private static final String BASE = "base";
    private static final String FINDINGS_FILE = "findings.txt";
    private static final List<String> FINDING_MARKERS = List.of("ali:missing", "Not implemented");

    public static void run(GameTestHelper helper, AbstractServer aliServer) {
        if (PluginManager.getInstance().clientRegistry == null) {
            PluginManager.getInstance().registerClientEvent();
        }

        String mod = System.getProperty("alicompat.gametest.mod", BASE);
        List<String> modIds = Arrays.stream(System.getProperty("alicompat.gametest.modids", "").split(","))
                .filter((modId) -> !modId.isBlank())
                .toList();

        if (!modIds.isEmpty() && modIds.stream().noneMatch(Services.getPlatform()::isModLoaded)) {
            throw new GameTestAssertException(mod + ": none of the target mods loaded: " + modIds);
        }

        GoldenLanguage.inject(namespaces());

        AliConfig config = PluginManager.getInstance().commonRegistry.getConfiguration();
        GoldenFiles golden = GoldenFiles.fromProperties(Utils.MOD_ID);
        Map<String, String> actual = LootDataCapture.capture(aliServer, config);
        Map<String, String> files = BASE.equals(mod) ? actual : GoldenFiles.diff(golden.resolve(BASE).expected(), actual);
        String findings = findings(files);

        if (!findings.isEmpty()) {
            files = new TreeMap<>(files);
            files.put(FINDINGS_FILE, findings);
        }

        List<String> differences = golden.resolve(mod).compare(files);

        if (!differences.isEmpty()) {
            throw new GameTestAssertException(mod + ": " + differences.size() + " golden difference(s): " + differences.subList(0, Math.min(10, differences.size())));
        }

        helper.succeed();
    }

    @NotNull
    private static Set<String> namespaces() {
        Set<String> namespaces = new TreeSet<>(List.of("minecraft", "aci", "ali", Utils.MOD_ID));

        Stream.of(BuiltInRegistries.ITEM, BuiltInRegistries.BLOCK, BuiltInRegistries.ENTITY_TYPE)
                .flatMap((registry) -> registry.keySet().stream())
                .forEach((location) -> namespaces.add(location.getNamespace()));
        return namespaces;
    }

    @NotNull
    private static String findings(Map<String, String> files) {
        StringBuilder builder = new StringBuilder();

        files.forEach((key, value) -> {
            if (!key.startsWith("advanced/")) {
                return;
            }

            List<String> lines = new ArrayList<>();

            for (String line : value.split("\n")) {
                if (FINDING_MARKERS.stream().anyMatch(line::contains)) {
                    lines.add(line.strip());
                }
            }

            if (!lines.isEmpty()) {
                builder.append(key).append('\n');
                lines.stream().distinct().forEach((line) -> builder.append("  ").append(line).append('\n'));
            }
        });

        return builder.toString();
    }
}
