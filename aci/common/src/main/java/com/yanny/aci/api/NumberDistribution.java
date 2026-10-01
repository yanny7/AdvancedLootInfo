package com.yanny.aci.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public final class NumberDistribution {
    public static final int MAX_ATOMS = 1024;

    private static final double EPSILON = 1e-9;
    private static final double TIE = 1e-6;
    private static final double KEY_SCALE = 1e9;

    private final TreeMap<Double, Double> atoms;
    @Nullable
    private final Density density;

    public record Density(DoubleUnaryOperator cdf, double lo, double hi, double mass,
                          double peakLo, double peakHi, double peakDensity, boolean flat) {
        public boolean hasPeak() {
            return !Double.isNaN(peakDensity);
        }
    }

    public record Weight(double weight, @Nullable NumberDistribution distribution) {
    }

    public enum Rounding {
        FLOOR, CEIL, ROUND, TRUNC;

        public double apply(double value) {
            return switch (this) {
                case FLOOR -> Math.floor(value);
                case CEIL -> Math.ceil(value);
                case ROUND -> Math.floor(value + 0.5);
                case TRUNC -> value < 0 ? Math.ceil(value) : Math.floor(value);
            };
        }

        private double[] cell(long k) {
            return switch (this) {
                case FLOOR -> new double[]{k, k + 1};
                case CEIL -> new double[]{k - 1, k};
                case ROUND -> new double[]{k - 0.5, k + 0.5};
                case TRUNC -> {
                    if (k > 0) {
                        yield new double[]{k, k + 1};
                    } else if (k < 0) {
                        yield new double[]{k - 1, k};
                    }

                    yield new double[]{-1, 1};
                }
            };
        }
    }

    private NumberDistribution(TreeMap<Double, Double> atoms, @Nullable Density density) {
        this.atoms = atoms;
        this.density = density;
    }

    @NotNull
    public static NumberDistribution constant(double value) {
        TreeMap<Double, Double> atoms = new TreeMap<>();

        put(atoms, value, 1);
        return new NumberDistribution(atoms, null);
    }

    @Nullable
    public static NumberDistribution discrete(Map<Double, Double> probabilities) {
        TreeMap<Double, Double> atoms = new TreeMap<>();

        probabilities.forEach((v, p) -> {
            if (p > 0) {
                put(atoms, v, p);
            }
        });
        return atoms.size() > MAX_ATOMS ? null : new NumberDistribution(atoms, null);
    }

    @NotNull
    public static NumberDistribution continuous(Density density) {
        return new NumberDistribution(new TreeMap<>(), density);
    }

    @Nullable
    public static NumberDistribution uniformInt(long min, long max) {
        if (min >= max) {
            return constant(min);
        }
        if (max - min + 1 > MAX_ATOMS) {
            return null;
        }

        TreeMap<Double, Double> atoms = new TreeMap<>();
        double p = 1.0 / (max - min + 1);

        for (long v = min; v <= max; v++) {
            atoms.put((double) v, p);
        }

        return new NumberDistribution(atoms, null);
    }

    @NotNull
    public static NumberDistribution uniformFloat(double min, double max) {
        if (min >= max) {
            return constant(min);
        }

        double width = max - min;
        DoubleUnaryOperator cdf = (x) -> Math.min(Math.max((x - min) / width, 0), 1);

        return continuous(new Density(cdf, min, max, 1, min, max, 1 / width, true));
    }

    @Nullable
    public static NumberDistribution ofParameters(List<NumberDistribution> parameters, Function<double[], NumberDistribution> family) {
        List<Weight> parts = new ArrayList<>();
        int combinations = 1;

        for (NumberDistribution parameter : parameters) {
            if (!parameter.isDiscrete()) {
                return null;
            }

            combinations *= parameter.atoms.size();

            if (combinations > MAX_ATOMS) {
                return null;
            }
        }

        collect(parameters, 0, new double[parameters.size()], 1, family, parts);
        return mixture(parts);
    }

    @Nullable
    public static NumberDistribution mixture(List<Weight> parts) {
        TreeMap<Double, Double> atoms = new TreeMap<>();
        List<WeightedDensity> continuous = new ArrayList<>();

        for (Weight part : parts) {
            NumberDistribution distribution = part.distribution;

            if (distribution == null) {
                return null;
            }

            distribution.atoms.forEach((v, p) -> put(atoms, v, p * part.weight));

            if (distribution.density != null) {
                continuous.add(new WeightedDensity(part.weight, distribution.density));
            }
        }

        if (atoms.size() > MAX_ATOMS) {
            return null;
        }
        if (continuous.isEmpty()) {
            return new NumberDistribution(atoms, null);
        }

        List<WeightedDensity> continuousParts = List.copyOf(continuous);
        DoubleUnaryOperator cdf = (x) -> continuousParts.stream()
                .mapToDouble((w) -> w.weight * w.density.cdf.applyAsDouble(x))
                .sum();
        double lo = continuousParts.stream().mapToDouble((w) -> w.density.lo).min().orElseThrow();
        double hi = continuousParts.stream().mapToDouble((w) -> w.density.hi).max().orElseThrow();
        double mass = continuousParts.stream().mapToDouble((w) -> w.weight * w.density.mass).sum();

        if (continuousParts.size() == 1) {
            Density d = continuousParts.get(0).density;
            double weight = continuousParts.get(0).weight;

            return new NumberDistribution(atoms, new Density(cdf, lo, hi, mass, d.peakLo, d.peakHi, d.peakDensity * weight, d.flat));
        }

        return new NumberDistribution(atoms, new Density(cdf, lo, hi, mass, Double.NaN, Double.NaN, Double.NaN, false));
    }

    @NotNull
    public SortedMap<Double, Double> atoms() {
        return Collections.unmodifiableSortedMap(atoms);
    }

    @Nullable
    public Density density() {
        return density;
    }

    public boolean isDiscrete() {
        return density == null;
    }

    @NotNull
    public OptionalDouble single() {
        return density == null && atoms.size() == 1 ? OptionalDouble.of(atoms.firstKey()) : OptionalDouble.empty();
    }

    @Nullable
    public NumberDistribution plus(NumberDistribution other) {
        if (density != null && other.density != null) {
            return null;
        }

        TreeMap<Double, Double> sum = new TreeMap<>();

        for (Map.Entry<Double, Double> x : atoms.entrySet()) {
            for (Map.Entry<Double, Double> y : other.atoms.entrySet()) {
                put(sum, x.getKey() + y.getKey(), x.getValue() * y.getValue());
            }
        }

        if (sum.size() > MAX_ATOMS) {
            return null;
        }

        Density continuous = density != null ? shiftMixture(density, other.atoms) : null;

        if (other.density != null) {
            continuous = shiftMixture(other.density, atoms);
        }

        return new NumberDistribution(sum, continuous);
    }

    @NotNull
    public NumberDistribution affine(double scale, double offset) {
        TreeMap<Double, Double> mapped = new TreeMap<>();

        atoms.forEach((v, p) -> put(mapped, v * scale + offset, p));

        Density c = density;

        if (c == null) {
            return new NumberDistribution(mapped, null);
        }
        if (scale == 0) {
            put(mapped, offset, c.mass);
            return new NumberDistribution(mapped, null);
        }

        DoubleUnaryOperator cdf;

        if (scale > 0) {
            cdf = (x) -> c.cdf.applyAsDouble((x - offset) / scale);
        } else {
            cdf = (x) -> c.mass - c.cdf.applyAsDouble((x - offset) / scale);
        }

        double lo = Math.min(c.lo * scale, c.hi * scale) + offset;
        double hi = Math.max(c.lo * scale, c.hi * scale) + offset;
        double peakLo = Math.min(c.peakLo * scale, c.peakHi * scale) + offset;
        double peakHi = Math.max(c.peakLo * scale, c.peakHi * scale) + offset;

        return new NumberDistribution(mapped, new Density(cdf, lo, hi, c.mass, peakLo, peakHi, c.peakDensity / Math.abs(scale), c.flat));
    }

    @Nullable
    public NumberDistribution times(NumberDistribution other) {
        OptionalDouble a = single();
        OptionalDouble b = other.single();

        if (a.isPresent()) {
            return other.affine(a.getAsDouble(), 0);
        }
        if (b.isPresent()) {
            return affine(b.getAsDouble(), 0);
        }

        return combine(other, (x, y) -> x * y);
    }

    @Nullable
    public NumberDistribution dividedBy(NumberDistribution other) {
        OptionalDouble b = other.single();

        if (b.isPresent() && b.getAsDouble() != 0) {
            return affine(1 / b.getAsDouble(), 0);
        }
        if (other.isDiscrete() && other.atoms.containsKey(0.0)) {
            return null;
        }

        return combine(other, (x, y) -> x / y);
    }

    @Nullable
    public NumberDistribution min(NumberDistribution other) {
        return extreme(other, true);
    }

    @Nullable
    public NumberDistribution max(NumberDistribution other) {
        return extreme(other, false);
    }

    @NotNull
    public NumberDistribution clamp(double lo, double hi) {
        TreeMap<Double, Double> clamped = new TreeMap<>();

        atoms.forEach((v, p) -> put(clamped, Math.min(Math.max(v, lo), hi), p));

        Density c = density;

        if (c == null) {
            return new NumberDistribution(clamped, null);
        }

        double below = Double.isInfinite(lo) ? 0 : c.cdf.applyAsDouble(lo);
        double above = Double.isInfinite(hi) ? 0 : c.mass - c.cdf.applyAsDouble(hi);

        if (below > 0) {
            put(clamped, lo, below);
        }
        if (above > 0) {
            put(clamped, hi, above);
        }

        double mass = c.mass - below - above;

        if (mass <= EPSILON) {
            return new NumberDistribution(clamped, null);
        }

        DoubleUnaryOperator cdf = (x) -> Math.min(Math.max(c.cdf.applyAsDouble(x) - below, 0), mass);
        boolean peakInside = c.hasPeak() && c.peakHi >= lo && c.peakLo <= hi;
        double peakLo = peakInside ? Math.max(c.peakLo, lo) : Double.NaN;
        double peakHi = peakInside ? Math.min(c.peakHi, hi) : Double.NaN;
        double peakDensity = peakInside ? c.peakDensity : Double.NaN;

        return new NumberDistribution(clamped, new Density(cdf, Math.max(c.lo, lo), Math.min(c.hi, hi), mass, peakLo, peakHi, peakDensity, c.flat));
    }

    @Nullable
    public NumberDistribution clamp(NumberDistribution min, NumberDistribution max) {
        OptionalDouble lo = min.single();
        OptionalDouble hi = max.single();

        if (lo.isPresent() && hi.isPresent()) {
            return clamp(lo.getAsDouble(), hi.getAsDouble());
        }

        NumberDistribution raised = combine(min, Math::max);

        return raised != null ? raised.combine(max, Math::min) : null;
    }

    @Nullable
    public NumberDistribution round(Rounding rounding) {
        TreeMap<Double, Double> rounded = new TreeMap<>();

        atoms.forEach((v, p) -> put(rounded, rounding.apply(v), p));

        Density c = density;

        if (c == null) {
            return new NumberDistribution(rounded, null);
        }
        if (Double.isInfinite(c.lo) || Double.isInfinite(c.hi)) {
            return null;
        }

        long from = (long) rounding.apply(c.lo) - 1;
        long to = (long) rounding.apply(c.hi) + 1;

        if (to - from > MAX_ATOMS) {
            return null;
        }

        for (long k = from; k <= to; k++) {
            double[] cell = rounding.cell(k);
            double mass = c.cdf.applyAsDouble(cell[1]) - c.cdf.applyAsDouble(cell[0]);

            if (mass > EPSILON * EPSILON) {
                put(rounded, k, mass);
            }
        }

        return rounded.size() > MAX_ATOMS ? null : new NumberDistribution(rounded, null);
    }

    @Nullable
    public NumberDistribution abs() {
        if (density != null && density.lo >= 0) {
            return this;
        }
        if (density != null && density.hi <= 0) {
            return affine(-1, 0);
        }

        return map(Math::abs);
    }

    @Nullable
    public NumberDistribution map(DoubleUnaryOperator f) {
        if (!isDiscrete()) {
            return null;
        }

        TreeMap<Double, Double> mapped = new TreeMap<>();

        atoms.forEach((v, p) -> put(mapped, f.applyAsDouble(v), p));
        return new NumberDistribution(mapped, null);
    }

    /** Distribution of {@code f(x, y)} for independent discrete {@code x} from this and {@code y} from {@code other}. */
    @Nullable
    public NumberDistribution combine(NumberDistribution other, DoubleBinaryOperator f) {
        if (!isDiscrete() || !other.isDiscrete()) {
            return null;
        }

        TreeMap<Double, Double> combined = new TreeMap<>();

        for (Map.Entry<Double, Double> x : atoms.entrySet()) {
            for (Map.Entry<Double, Double> y : other.atoms.entrySet()) {
                put(combined, f.applyAsDouble(x.getKey(), y.getKey()), x.getValue() * y.getValue());
            }

            if (combined.size() > MAX_ATOMS) {
                return null;
            }
        }

        return new NumberDistribution(combined, null);
    }

    @NotNull
    public Optional<NumberMode> mode() {
        if (density == null) {
            return discreteMode(true);
        }
        if (!density.hasPeak()) {
            return Optional.empty();
        }

        double maxAtom = atoms.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);

        if (maxAtom > density.peakDensity) {
            return discreteMode(false);
        }
        if (density.flat) {
            return Optional.empty();
        }

        return Optional.of(new NumberMode(density.peakLo, density.peakHi, Double.NaN));
    }

    @Nullable
    private NumberDistribution extreme(NumberDistribution other, boolean min) {
        OptionalDouble a = single();
        OptionalDouble b = other.single();

        if (b.isPresent()) {
            return min ? clamp(Double.NEGATIVE_INFINITY, b.getAsDouble()) : clamp(b.getAsDouble(), Double.POSITIVE_INFINITY);
        }
        if (a.isPresent()) {
            return min ? other.clamp(Double.NEGATIVE_INFINITY, a.getAsDouble()) : other.clamp(a.getAsDouble(), Double.POSITIVE_INFINITY);
        }

        return combine(other, min ? Math::min : Math::max);
    }

    @NotNull
    private Optional<NumberMode> discreteMode(boolean requireDifference) {
        if (atoms.isEmpty()) {
            return Optional.empty();
        }

        double max = atoms.values().stream().mapToDouble(Double::doubleValue).max().orElseThrow();
        double min = atoms.values().stream().mapToDouble(Double::doubleValue).min().orElseThrow();

        if (requireDifference && max - min <= TIE * max) {
            return Optional.empty();
        }

        boolean integers = atoms.keySet().stream().allMatch((k) -> k == Math.rint(k));
        double first = 0;
        double last = 0;
        boolean started = false;
        boolean ended = false;

        for (Map.Entry<Double, Double> entry : atoms.entrySet()) {
            boolean best = max - entry.getValue() <= TIE * max;

            if (best && (ended || (started && integers && entry.getKey() - last > 1))) {
                return Optional.empty();
            }
            if (best) {
                if (!started) {
                    first = entry.getKey();
                    started = true;
                }

                last = entry.getKey();
            } else if (started) {
                ended = true;
            }
        }

        return Optional.of(new NumberMode(first, last, max));
    }

    @Nullable
    private static Density shiftMixture(Density c, TreeMap<Double, Double> shifts) {
        if (shifts.isEmpty()) {
            return null;
        }

        double[] values = shifts.keySet().stream().mapToDouble(Double::doubleValue).toArray();
        double[] weights = shifts.values().stream().mapToDouble(Double::doubleValue).toArray();
        double weight = 0;

        for (double w : weights) {
            weight += w;
        }

        DoubleUnaryOperator cdf = (x) -> {
            double sum = 0;

            for (int i = 0; i < values.length; i++) {
                sum += weights[i] * c.cdf.applyAsDouble(x - values[i]);
            }

            return sum;
        };

        if (values.length == 1) {
            double s = values[0];

            return new Density(cdf, c.lo + s, c.hi + s, c.mass * weight, c.peakLo + s, c.peakHi + s, c.peakDensity * weight, c.flat);
        }

        return new Density(cdf, c.lo + values[0], c.hi + values[values.length - 1], c.mass * weight, Double.NaN, Double.NaN, Double.NaN, false);
    }

    private static void collect(List<NumberDistribution> parameters, int index, double[] values, double probability,
                                Function<double[], NumberDistribution> family, List<Weight> parts) {
        if (index == parameters.size()) {
            parts.add(new Weight(probability, family.apply(values.clone())));
            return;
        }

        for (Map.Entry<Double, Double> entry : parameters.get(index).atoms.entrySet()) {
            values[index] = entry.getKey();
            collect(parameters, index + 1, values, probability * entry.getValue(), family, parts);
        }
    }

    private static void put(TreeMap<Double, Double> atoms, double value, double probability) {
        double key = Math.rint(value * KEY_SCALE) / KEY_SCALE;

        atoms.merge(key == 0 ? 0.0 : key, probability, Double::sum);
    }

    private record WeightedDensity(double weight, Density density) {
    }
}
