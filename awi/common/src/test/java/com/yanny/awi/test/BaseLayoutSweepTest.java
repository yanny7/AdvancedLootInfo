package com.yanny.awi.test;

import com.yanny.awi.plugin.common.nodes.NodeUtils;
import com.yanny.awi.test.utils.BaseLayoutTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cost/coverage sweep for the scan's sampling knobs — the tool for deciding whether a cheaper
 * {@link NodeUtils.ScanSettings} is safe. Not a regression test: it asserts nothing, it reports.
 * <p>
 * For every candidate setting it scans all vanilla biomes over the same seeds as {@link BaseLayoutTest} and prints how
 * long it took plus what changed against the current production settings — blocks that disappeared (coverage lost) and
 * blocks that appeared (coverage gained). Only the block set is diffed, not the exact ranges, because the ranges of
 * noise-driven blocks legitimately move with the sampling.
 * <p>
 * Run with: {@code ./gradlew :awi:common:test --tests "com.yanny.awi.test.BaseLayoutSweepTest" -Dawi.baselayout.sweep=true}
 */
@EnabledIfSystemProperty(named = "awi.baselayout.sweep", matches = "true")
public class BaseLayoutSweepTest {
    private static final Pattern RANGE = Pattern.compile("(-?\\d+)(?:-(-?\\d+))?");
    private static final List<Long> SEEDS = List.of(1234L, 987654321L, -42L);
    private static final Map<String, NodeUtils.ScanSettings> CANDIDATES = new LinkedHashMap<>() {{
        put("specialization off", settings(8, 4, 8, 12, 8, 32, false));
        put("half columns", settings(4, 4, 8, 12, 8, 32, true));
    }};

    private static NodeUtils.ScanSettings settings(int columns, int step, int stable, int extentStable, int ceiling, int window, boolean specialize) {
        return new NodeUtils.ScanSettings(columns, step, stable, extentStable, NodeUtils.ScanSettings.DEFAULT.maxRounds(), ceiling, window, specialize);
    }

    @BeforeAll
    static void setUp() {
        BaseLayoutTestUtils.bootstrap();
    }

    @Test
    public void sweepScanSettings() {
        // Warm-up: without it the first timed scan pays the JIT cost and every later candidate looks ~20% faster than
        // it is — enough to rank a slower setting as an improvement.
        long warmupStart = System.currentTimeMillis();

        coveragePerBiome(NodeUtils.ScanSettings.DEFAULT);
        System.out.printf("%nwarm-up: %dms%n", System.currentTimeMillis() - warmupStart);

        long referenceStart = System.currentTimeMillis();
        Map<String, Set<String>> reference = coveragePerBiome(NodeUtils.ScanSettings.DEFAULT);
        long referenceTime = System.currentTimeMillis() - referenceStart;

        System.out.printf("%nreference %s: %dms, %d covered positions over %d biome scans%n%n",
                NodeUtils.ScanSettings.DEFAULT, referenceTime, countAtoms(reference), reference.size());

        CANDIDATES.forEach((name, candidate) -> {
            long start = System.currentTimeMillis();
            Map<String, Set<String>> result = coveragePerBiome(candidate);
            long duration = System.currentTimeMillis() - start;

            List<String> lost = diff(reference, result);
            List<String> gained = diff(result, reference);

            System.out.printf("%s %s%n  %dms (%.0f%% of reference), %d lost, %d gained (biome+block groups)%n",
                    name, candidate, duration, 100.0 * duration / referenceTime, lost.size(), gained.size());
            lost.forEach((entry) -> System.out.println("  - " + entry));
            gained.forEach((entry) -> System.out.println("  + " + entry));
            System.out.println();
        });
    }

    /**
     * Expands every discovered block into atomic {@code (block, storage, position, water, placement)} tuples, so the
     * diff catches not just a missing block but also a narrowed range or a lost flag: {@code ANY} water covers both
     * DRY and UNDERWATER, {@code ANY} placement covers both FLOOR and CEILING, so splitting {@code ANY} into its two
     * halves is not a loss while replacing it with only one half is.
     */
    private static Map<String, Set<String>> coveragePerBiome(NodeUtils.ScanSettings settings) {
        Map<String, Set<String>> coverage = new LinkedHashMap<>();

        for (long seed : SEEDS) {
            BaseLayoutTestUtils.scan(seed, settings).forEach((dimension, biomes) -> biomes.forEach((biome, lines) -> {
                Set<String> atoms = new TreeSet<>();

                lines.forEach((line) -> atoms.addAll(atoms(line)));
                coverage.put("seed=%d %s %s".formatted(seed, dimension, biome), atoms);
            }));
        }

        return coverage;
    }

    /** {@code "minecraft:dirt RELATIVE [0-4] ANY FLOOR"} -> one atom per position/water/placement combination. */
    private static List<String> atoms(String line) {
        String[] parts = line.split(" ", 3);
        String body = parts[2];
        String ranges = body.substring(body.indexOf('[') + 1, body.lastIndexOf(']'));
        String[] flags = body.substring(body.lastIndexOf(']') + 2).split(" ");
        List<String> waters = "ANY".equals(flags[0]) ? List.of("DRY", "UNDERWATER") : List.of(flags[0]);
        List<String> placements = "ANY".equals(flags[1]) ? List.of("FLOOR", "CEILING") : List.of(flags[1]);
        List<String> result = new ArrayList<>();

        for (String range : ranges.isBlank() ? new String[0] : ranges.split(",")) {
            Matcher matcher = RANGE.matcher(range.trim());

            if (!matcher.matches()) {
                throw new IllegalStateException("Unparseable range '" + range + "' in " + line);
            }

            int from = Integer.parseInt(matcher.group(1));
            int to = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : from;

            for (int position = Math.min(from, to); position <= Math.max(from, to); position++) {
                for (String water : waters) {
                    for (String placement : placements) {
                        result.add("%s %s %d %s %s".formatted(parts[0], parts[1], position, water, placement));
                    }
                }
            }
        }

        return result;
    }

    /** Atoms present in {@code from} but not in {@code to}, summarised per biome+block (bands are noisy by nature). */
    private static List<String> diff(Map<String, Set<String>> from, Map<String, Set<String>> to) {
        Map<String, Integer> counts = new TreeMap<>();

        from.forEach((biome, atoms) -> atoms.stream()
                .filter((atom) -> !to.getOrDefault(biome, Set.of()).contains(atom))
                .forEach((atom) -> counts.merge(biome + ": " + atom.split(" ")[0], 1, Integer::sum)));

        return counts.entrySet().stream().map((entry) -> "%s (%d positions)".formatted(entry.getKey(), entry.getValue())).toList();
    }

    private static long countAtoms(Map<String, Set<String>> coverage) {
        return coverage.values().stream().mapToLong(Set::size).sum();
    }
}
