package com.yanny.awi.compatibility;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberDistribution;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.awi.Utils;
import com.yanny.awi.plugin.common.nodes.BlockNode;
import com.yanny.awi.plugin.common.nodes.PlacedFeatureNode;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;

public record BlockGenerationChart(int minY, int binSize, int bins, int biomeCount, List<Series> series, List<Column> columns,
                                   Map<BlockGeneration.Source, Exclusion> exclusions) {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final int TARGET_BINS = 96;
    private static final int INTEGRATION_STEPS = 256;

    public enum Exclusion {
        BASE_TERRAIN,
        NO_PLACEMENT,
        SURFACE,
        UNKNOWN_HEIGHT,
        UNKNOWN_COUNT,
        OUTSIDE_WORLD,
    }

    public record Series(BlockGeneration.Source source, List<Identifier> biomes, double[] values, boolean peaked) {}

    public record Column(List<Identifier> biomes, boolean allBiomes, List<Series> series, double max, boolean terrain) {
        public double total(int bin) {
            return series.stream().mapToDouble((s) -> s.values[bin]).sum();
        }
    }

    public int binMinY(int bin) {
        return minY + bin * binSize;
    }

    @NotNull
    public static BlockGenerationChart of(BlockGeneration.Dimension dimension) {
        int binSize = Math.max(1, (int) Math.ceil(dimension.height() / (double) TARGET_BINS));
        int bins = (int) Math.ceil(dimension.height() / (double) binSize);
        List<Series> series = new ArrayList<>();
        List<Series> terrain = new ArrayList<>();
        Map<BlockGeneration.Source, Exclusion> exclusions = new IdentityHashMap<>();
        Map<Identifier, List<Series>> byBiome = new TreeMap<>();
        Map<List<Series>, List<Identifier>> profiles = new LinkedHashMap<>();
        List<Column> columns = new ArrayList<>();

        for (BlockGeneration.BiomeGroup group : Stream.concat(dimension.rows().stream(), dimension.merged().stream()).toList()) {
            for (BlockGeneration.Source source : group.sources()) {
                double[] values = new double[bins];
                Exclusion exclusion = source.feature() == null ? presence(source, dimension.minY(), binSize, values) : values(source, dimension.minY(), binSize, values);

                if (exclusion != null) {
                    exclusions.put(source, exclusion);
                    continue;
                }

                Series s = new Series(source, group.biomes(), values, source.feature() != null && peaked(source));

                series.add(s);

                if (source.feature() == null) {
                    terrain.add(s);
                    continue;
                }

                for (Identifier biome : group.biomes()) {
                    byBiome.computeIfAbsent(biome, (b) -> new ArrayList<>()).add(s);
                }
            }
        }

        byBiome.forEach((biome, profile) -> profiles.computeIfAbsent(profile, (p) -> new ArrayList<>()).add(biome));
        profiles.forEach((profile, biomes) -> columns.add(column(biomes, biomes.size() == dimension.biomeCount(), profile, bins)));

        if (!terrain.isEmpty()) {
            List<Identifier> biomes = new ArrayList<>(terrain.stream().flatMap((t) -> t.biomes().stream()).collect(TreeSet::new, TreeSet::add, TreeSet::addAll));

            columns.add(new Column(biomes, biomes.size() == dimension.biomeCount(), terrain, 1, true));
        }

        columns.sort(Comparator.comparing((Column c) -> !c.terrain())
                .thenComparingInt((c) -> c.series().stream().mapToInt(series::indexOf).min().orElse(0))
                .thenComparing((c) -> -c.biomes().size())
                .thenComparing((c) -> c.biomes().get(0)));
        return new BlockGenerationChart(dimension.minY(), binSize, bins, dimension.biomeCount(), series, columns, exclusions);
    }

    @NotNull
    private static Column column(List<Identifier> biomes, boolean allBiomes, List<Series> series, int bins) {
        List<Series> stack = series.stream().sorted(Comparator.comparing(Series::peaked)).toList();
        Column column = new Column(biomes, allBiomes, stack, 0, false);
        double max = 0;

        for (int i = 0; i < bins; i++) {
            max = Math.max(max, column.total(i));
        }

        return new Column(biomes, allBiomes, stack, max, false);
    }

    private static boolean peaked(BlockGeneration.Source source) {
        PlacedFeatureNode.Stats stats = Objects.requireNonNull(Objects.requireNonNull(source.feature()).getStats());
        NumberDistribution distribution = stats.height() != null ? stats.height().distribution() : null;

        return distribution != null && distribution.mode().isPresent();
    }

    @Nullable
    private static Exclusion presence(BlockGeneration.Source source, int minY, int binSize, double[] values) {
        List<NumberInterval> heights = source.blockNodes().stream().flatMap((n) -> n instanceof BlockNode blockNode ? blockNode.getHeights().stream() : Stream.empty()).toList();
        boolean any = false;

        if (heights.isEmpty()) {
            return Exclusion.BASE_TERRAIN;
        }

        for (int i = 0; i < values.length; i++) {
            int from = minY + i * binSize;
            int to = from + binSize;

            for (NumberInterval height : heights) {
                double lo = height.loClosed() ? Math.ceil(height.lo()) : Math.floor(height.lo()) + 1;
                double hi = height.hiClosed() ? Math.floor(height.hi()) : Math.ceil(height.hi()) - 1;

                if (lo < to && hi >= from) {
                    values[i] = 1;
                    any = true;
                    break;
                }
            }
        }

        return any ? null : Exclusion.OUTSIDE_WORLD;
    }

    @Nullable
    private static Exclusion values(BlockGeneration.Source source, int minY, int binSize, double[] values) {
        PlacedFeatureNode feature = Objects.requireNonNull(source.feature());
        PlacedFeatureNode.Stats stats = feature.getStats();

        if (stats == null) {
            return Exclusion.NO_PLACEMENT;
        } else if (stats.surface()) {
            return Exclusion.SURFACE;
        } else if (stats.height() == null) {
            return Exclusion.UNKNOWN_HEIGHT;
        }

        NumberDistribution distribution = stats.height().distribution();
        NumberInterval bounds = stats.height().bounds();

        if (distribution == null && !bounds.isBounded()) {
            return Exclusion.UNKNOWN_HEIGHT;
        }

        double weight;

        try {
            weight = mean(stats.count()) * mean(stats.chance());
        } catch (Throwable e) {
            LOGGER.warn("Failed to chart count of {}: {}", feature.getFeatureId(), e.getMessage());
            return Exclusion.UNKNOWN_COUNT;
        }

        if (!Double.isFinite(weight) || weight <= 0) {
            return Exclusion.UNKNOWN_COUNT;
        }

        try {
            for (int i = 0; i < values.length; i++) {
                values[i] = weight * mass(distribution, bounds, minY + i * binSize, minY + (i + 1) * binSize);
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to chart height of {}: {}", feature.getFeatureId(), e.getMessage());
            return Exclusion.UNKNOWN_HEIGHT;
        }

        return Arrays.stream(values).sum() > 0 ? null : Exclusion.OUTSIDE_WORLD;
    }

    private static double mean(@Nullable NumberExpr expr) {
        if (expr == null) {
            return 1;
        }

        NumberDistribution distribution = expr.distribution();

        if (distribution != null) {
            double sum = 0;

            for (Map.Entry<Double, Double> atom : distribution.atoms().entrySet()) {
                sum += atom.getKey() * atom.getValue();
            }

            NumberDistribution.Density density = distribution.density();

            if (density != null) {
                if (!Double.isFinite(density.lo()) || !Double.isFinite(density.hi())) {
                    return Double.NaN;
                }

                sum += density.hi() * density.mass() - integrate(density.cdf(), density.lo(), density.hi());
            }

            return sum;
        }

        NumberInterval bounds = expr.bounds();
        return bounds.isBounded() ? (bounds.lo() + bounds.hi()) / 2 : Double.NaN;
    }

    private static double mass(@Nullable NumberDistribution distribution, NumberInterval bounds, int from, int to) {
        if (distribution != null) {
            double sum = distribution.atoms().subMap((double) from, (double) to).values().stream().mapToDouble(Double::doubleValue).sum();
            NumberDistribution.Density density = distribution.density();

            if (density != null) {
                sum += density.cdf().applyAsDouble(to) - density.cdf().applyAsDouble(from);
            }

            return sum;
        }

        double lo = Math.ceil(bounds.lo());
        double hi = Math.floor(bounds.hi());
        double overlap = Math.min(hi + 1, to) - Math.max(lo, from);

        return overlap > 0 ? overlap / (hi - lo + 1) : 0;
    }

    private static double integrate(DoubleUnaryOperator f, double lo, double hi) {
        double step = (hi - lo) / INTEGRATION_STEPS;
        double sum = (f.applyAsDouble(lo) + f.applyAsDouble(hi)) / 2;

        for (int i = 1; i < INTEGRATION_STEPS; i++) {
            sum += f.applyAsDouble(lo + i * step);
        }

        return sum * step;
    }
}
