package com.yanny.awi.test;

import com.yanny.awi.plugin.common.nodes.BaseLayoutScanner;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
import com.yanny.awi.test.utils.BaseLayoutTestUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EnabledIfSystemProperty(named = "awi.baselayout.perf", matches = "true")
public class BaseLayoutPerfTest {
    private static final List<Long> SEEDS = List.of(1234L, 987654321L, -42L);
    private static final int TIMED_RUNS = 5;
    private static final Path REPORT = Path.of("build", "base_layout_perf.txt");
    private static final Pattern NUMBER = Pattern.compile("-?\\d+");

    @BeforeAll
    static void setUp() {
        BaseLayoutTestUtils.bootstrap();
    }

    @Test
    public void reportScanCost() throws IOException {
        NodeUtils.ScanSettings settings = NodeUtils.ScanSettings.DEFAULT;
        List<String> report = new ArrayList<>();

        report.add("settings " + settings);
        runAllSeeds(settings);

        List<Long> wallTimes = new ArrayList<>();
        List<Long> biomeTimes = new ArrayList<>();
        Map<Long, BaseLayoutScanner> scanners = Map.of();

        for (int run = 0; run < TIMED_RUNS; run++) {
            long start = System.nanoTime();

            scanners = runAllSeeds(settings);
            wallTimes.add((System.nanoTime() - start) / 1_000_000L);
            biomeTimes.add(scanners.values().stream()
                    .mapToLong((scanner) -> Math.round(scanner.getStats().meanBiomeTimeMs() * scanner.getStats().scannedBiomeCount()))
                    .sum());
        }

        report.add("");
        report.add("== time, %d seeds, %d runs after a warm-up ==".formatted(SEEDS.size(), TIMED_RUNS));
        report.add("wall ms      median %d, min %d, all %s".formatted(median(wallTimes), Collections.min(wallTimes), wallTimes));
        report.add("biome-sum ms median %d, min %d, all %s".formatted(median(biomeTimes), Collections.min(biomeTimes), biomeTimes));

        reportRounds(report, scanners);
        reportSeedStability(report, scanners);

        Files.write(REPORT, report, StandardCharsets.UTF_8);
        report.forEach(System.out::println);
        System.out.println("Written to " + REPORT.toAbsolutePath());
    }

    private static Map<Long, BaseLayoutScanner> runAllSeeds(NodeUtils.ScanSettings settings) {
        Map<Long, BaseLayoutScanner> scanners = new TreeMap<>();

        for (long seed : SEEDS) {
            scanners.put(seed, BaseLayoutTestUtils.scanner(seed, settings, false));
        }

        return scanners;
    }

    private static void reportRounds(List<String> report, Map<Long, BaseLayoutScanner> scanners) {
        Map<String, List<Integer>> roundsPerBiome = new TreeMap<>();
        Set<String> capped = new TreeSet<>();
        long total = 0;

        for (BaseLayoutScanner scanner : scanners.values()) {
            for (LevelStem levelStem : BaseLayoutTestUtils.levelStems()) {
                ResourceLocation dimension = BaseLayoutTestUtils.levelStems().getKey(levelStem);

                scanner.getBaseLayouts(dimension).forEach((biome, layers) -> {
                    String name = "%s %s".formatted(dimension, BaseLayoutTestUtils.biomeName(biome));

                    roundsPerBiome.computeIfAbsent(name, (k) -> new ArrayList<>()).add(layers.rounds());

                    if (layers.hitRoundCap()) {
                        capped.add(name);
                    }
                });
            }
        }

        for (List<Integer> rounds : roundsPerBiome.values()) {
            total += rounds.stream().mapToInt(Integer::intValue).sum();
        }

        report.add("");
        report.add("== rounds, per biome over seeds %s ==".formatted(SEEDS));
        report.add("total %d over %d biomes; round cap (%d) hit by %d: %s".formatted(total, roundsPerBiome.size(),
                NodeUtils.ScanSettings.DEFAULT.maxRounds(), capped.size(), capped));
        roundsPerBiome.entrySet().stream()
                .sorted(Map.Entry.<String, List<Integer>>comparingByValue(
                        (a, b) -> Integer.compare(Collections.max(b), Collections.max(a))).thenComparing(Map.Entry.comparingByKey()))
                .forEach((entry) -> report.add("  %-50s %s".formatted(entry.getKey(), entry.getValue())));
    }

    private static void reportSeedStability(List<String> report, Map<Long, BaseLayoutScanner> scanners) {
        Map<String, Map<Long, List<String>>> sites = new TreeMap<>();

        scanners.forEach((seed, scanner) -> BaseLayoutTestUtils.describe(scanner).forEach((dimension, biomes) ->
                biomes.forEach((biome, lines) -> lines.forEach((line) -> sites
                        .computeIfAbsent("%s %s %s".formatted(dimension, biome, line.split(" ", 2)[0]), (k) -> new TreeMap<>())
                        .computeIfAbsent(seed, (k) -> new ArrayList<>())
                        .add(line)))));

        List<String> unstable = new ArrayList<>();
        long numbers = 0;
        long unstableNumbers = 0;

        for (Map.Entry<String, Map<Long, List<String>>> site : sites.entrySet()) {
            Map<Long, List<String>> perSeed = site.getValue();
            long siteNumbers = 0;

            for (long seed : SEEDS) {
                siteNumbers += countNumbers(perSeed.getOrDefault(seed, List.of()));
            }

            numbers += siteNumbers;

            if (perSeed.size() < SEEDS.size() || perSeed.values().stream().distinct().count() > 1) {
                unstableNumbers += siteNumbers;
                unstable.add("  %s  %s".formatted(site.getKey(), SEEDS.stream()
                        .map((seed) -> perSeed.containsKey(seed) ? "present" : "ABSENT")
                        .toList()));
            }
        }

        report.add("");
        report.add("== seed stability over seeds %s ==".formatted(SEEDS));
        report.add("unstable sites %d of %d; %d of %d range numbers (%.0f%%) sit in unstable sites".formatted(
                unstable.size(), sites.size(), unstableNumbers, numbers, 100.0 * unstableNumbers / Math.max(1, numbers)));
        report.addAll(unstable);
    }

    private static long countNumbers(List<String> lines) {
        long count = 0;

        for (String line : lines) {
            Matcher matcher = NUMBER.matcher(line.substring(line.indexOf('['), line.indexOf(']')));

            while (matcher.find()) {
                count++;
            }
        }

        return count;
    }

    private static long median(List<Long> values) {
        List<Long> sorted = new ArrayList<>(values);

        Collections.sort(sorted);

        return sorted.get(sorted.size() / 2);
    }
}
