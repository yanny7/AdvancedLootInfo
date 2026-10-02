package com.yanny.aci.api;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;

public record NumberInterval(double lo, double hi, boolean loClosed, boolean hiClosed) {
    public static final NumberInterval UNKNOWN = new NumberInterval(Double.NaN, Double.NaN, false, false);
    public static final NumberInterval ALL = new NumberInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, false);

    public NumberInterval {
        if (Double.isInfinite(lo)) {
            loClosed = false;
        }
        if (Double.isInfinite(hi)) {
            hiClosed = false;
        }
    }

    @NotNull
    public static NumberInterval point(double value) {
        return closed(value, value);
    }

    @NotNull
    public static NumberInterval closed(double lo, double hi) {
        return new NumberInterval(lo, hi, true, true);
    }

    public boolean isUnknown() {
        return Double.isNaN(lo) || Double.isNaN(hi);
    }

    public boolean isPoint() {
        return !isUnknown() && lo == hi;
    }

    public boolean isBounded() {
        return !isUnknown() && Double.isFinite(lo) && Double.isFinite(hi);
    }

    @NotNull
    public NumberInterval union(NumberInterval other) {
        if (isUnknown() || other.isUnknown()) {
            return UNKNOWN;
        }

        double newLo = Math.min(lo, other.lo);
        double newHi = Math.max(hi, other.hi);

        return new NumberInterval(
                newLo,
                newHi,
                isClosedAt(newLo, lo, loClosed, other.lo, other.loClosed),
                isClosedAt(newHi, hi, hiClosed, other.hi, other.hiClosed)
        );
    }

    private static boolean isClosedAt(double value, double a, boolean aClosed, double b, boolean bClosed) {
        return (a == value && aClosed) || (b == value && bClosed);
    }

    @NotNull
    public NumberInterval clamp(double min, double max) {
        if (isUnknown()) {
            return this;
        }

        double newLo = Math.min(Math.max(lo, min), max);
        double newHi = Math.max(Math.min(hi, max), min);
        boolean newLoClosed = newLo != lo || loClosed;
        boolean newHiClosed = newHi != hi || hiClosed;

        return new NumberInterval(newLo, newHi, newLoClosed, newHiClosed);
    }

    @NotNull
    public NumberInterval plus(NumberInterval other) {
        return new NumberInterval(lo + other.lo, hi + other.hi, loClosed && other.loClosed, hiClosed && other.hiClosed);
    }

    @NotNull
    public NumberInterval negate() {
        return new NumberInterval(-hi, -lo, hiClosed, loClosed);
    }

    @NotNull
    public NumberInterval minus(NumberInterval other) {
        return plus(other.negate());
    }

    @NotNull
    public NumberInterval times(NumberInterval other) {
        return fromEndpoints(new Endpoint[]{
                product(lo, loClosed, other.lo, other.loClosed),
                product(lo, loClosed, other.hi, other.hiClosed),
                product(hi, hiClosed, other.lo, other.loClosed),
                product(hi, hiClosed, other.hi, other.hiClosed),
        });
    }

    @NotNull
    public NumberInterval dividedBy(NumberInterval other) {
        if (other.lo <= 0 && other.hi >= 0) {
            return ALL;
        }

        return times(new NumberInterval(1 / other.hi, 1 / other.lo, other.hiClosed, other.loClosed));
    }

    @NotNull
    public NumberInterval min(NumberInterval other) {
        double newLo = Math.min(lo, other.lo);
        double newHi = Math.min(hi, other.hi);

        return new NumberInterval(
                newLo,
                newHi,
                isClosedAt(newLo, lo, loClosed, other.lo, other.loClosed),
                isClosedAt(newHi, hi, hiClosed, other.hi, other.hiClosed)
        );
    }

    @NotNull
    public NumberInterval max(NumberInterval other) {
        return negate().min(other.negate()).negate();
    }

    @NotNull
    public NumberInterval abs() {
        if (lo >= 0) {
            return this;
        }
        if (hi <= 0) {
            return negate();
        }

        NumberInterval negated = negate();

        return new NumberInterval(0, Math.max(negated.hi, hi), true, negated.max(this).hiClosed);
    }

    @NotNull
    public NumberInterval square() {
        NumberInterval absolute = abs();

        return absolute.times(absolute);
    }

    @NotNull
    public NumberInterval sqrt() {
        return new NumberInterval(Math.sqrt(Math.max(0, lo)), Math.sqrt(hi), lo <= 0 || loClosed, hiClosed);
    }

    @NotNull
    public NumberInterval pow(NumberInterval exponent) {
        if (exponent.isPoint() && exponent.lo == Math.rint(exponent.lo) && exponent.lo >= 0) {
            double n = exponent.lo;

            if (n % 2 == 0) {
                return abs().monotone((x) -> Math.pow(x, n));
            }

            return monotone((x) -> Math.pow(x, n));
        }
        if (lo > 0) {
            return fromEndpoints(new Endpoint[]{
                    new Endpoint(Math.pow(lo, exponent.lo), loClosed && exponent.loClosed),
                    new Endpoint(Math.pow(lo, exponent.hi), loClosed && exponent.hiClosed),
                    new Endpoint(Math.pow(hi, exponent.lo), hiClosed && exponent.loClosed),
                    new Endpoint(Math.pow(hi, exponent.hi), hiClosed && exponent.hiClosed),
            });
        }

        return ALL;
    }

    @NotNull
    public NumberInterval mod(NumberInterval divisor) {
        double m = Math.max(Math.abs(divisor.lo), Math.abs(divisor.hi));
        NumberInterval positive = new NumberInterval(0, Math.min(Math.max(hi, 0), m), true, hi < m && hiClosed);
        NumberInterval negative = new NumberInterval(-Math.min(Math.max(-lo, 0), m), 0, -lo < m && loClosed, true);

        if (lo >= 0) {
            return positive;
        }
        if (hi <= 0) {
            return negative;
        }

        return negative.union(positive);
    }

    @NotNull
    public NumberInterval floorMod(NumberInterval divisor) {
        if (divisor.lo > 0) {
            if (lo >= 0 && hi < divisor.lo) {
                return this;
            }

            return new NumberInterval(0, divisor.hi, true, false);
        }
        if (divisor.hi < 0) {
            return new NumberInterval(divisor.lo, 0, false, true);
        }

        double m = Math.max(Math.abs(divisor.lo), Math.abs(divisor.hi));

        return new NumberInterval(-m, m, false, false);
    }

    @NotNull
    public NumberInterval floor() {
        double newHi = !hiClosed && hi == Math.floor(hi) ? hi - 1 : Math.floor(hi);

        return closed(Math.floor(lo), newHi);
    }

    @NotNull
    public NumberInterval ceil() {
        double newLo = !loClosed && lo == Math.ceil(lo) ? lo + 1 : Math.ceil(lo);

        return closed(newLo, Math.ceil(hi));
    }

    @NotNull
    public NumberInterval round() {
        return plus(point(0.5)).floor();
    }

    @NotNull
    public NumberInterval trunc() {
        double newLo = lo >= 0 ? Math.floor(lo) : new NumberInterval(lo, lo, loClosed, true).ceil().lo;
        double newHi = hi > 0 ? new NumberInterval(hi, hi, true, hiClosed).floor().hi : Math.ceil(hi);

        return closed(newLo, newHi);
    }

    /** {@code f} must be non-decreasing and continuous on this interval. */
    @NotNull
    public NumberInterval monotone(DoubleUnaryOperator f) {
        return new NumberInterval(f.applyAsDouble(lo), f.applyAsDouble(hi), loClosed, hiClosed);
    }

    @NotNull
    private static Endpoint product(double x, boolean xClosed, double y, boolean yClosed) {
        if ((x == 0 && xClosed) || (y == 0 && yClosed)) {
            return new Endpoint(0, true);
        }
        if (x == 0 || y == 0) {
            return new Endpoint(0, false);
        }

        return new Endpoint(x * y, xClosed && yClosed);
    }

    @NotNull
    private static NumberInterval fromEndpoints(Endpoint[] candidates) {
        double newLo = Double.POSITIVE_INFINITY;
        double newHi = Double.NEGATIVE_INFINITY;

        for (Endpoint e : candidates) {
            newLo = Math.min(newLo, e.value);
            newHi = Math.max(newHi, e.value);
        }

        boolean newLoClosed = false;
        boolean newHiClosed = false;

        for (Endpoint e : candidates) {
            newLoClosed |= e.value == newLo && e.closed;
            newHiClosed |= e.value == newHi && e.closed;
        }

        return new NumberInterval(newLo, newHi, newLoClosed, newHiClosed);
    }

    private record Endpoint(double value, boolean closed) {
    }
}
