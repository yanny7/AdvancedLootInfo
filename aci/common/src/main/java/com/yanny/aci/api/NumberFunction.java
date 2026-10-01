package com.yanny.aci.api;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public interface NumberFunction {
    int ADDITIVE = 1;
    int MULTIPLICATIVE = 2;
    int UNARY = 3;

    enum Unit {
        SAME,
        PLAIN,
        PERCENT,
    }

    enum InfixKind {
        ASSOCIATIVE,
        LEFT,
        PREFIX,
    }

    record Infix(String symbol, int precedence, InfixKind kind) {
    }

    @NotNull
    Identifier id();

    default int minArity() {
        return 0;
    }

    default int maxArity() {
        return Integer.MAX_VALUE;
    }

    @Nullable
    default Infix infix() {
        return null;
    }

    @NotNull
    default Unit unit(int argument) {
        return Unit.SAME;
    }

    @Nullable
    default NumberExpr simplify(List<NumberExpr> args) {
        return null;
    }

    @Nullable
    default NumberExpr expand(List<NumberExpr> args) {
        return null;
    }

    @NotNull
    default OptionalDouble evaluate(double[] args) {
        return OptionalDouble.empty();
    }

    @NotNull
    default NumberInterval bounds(List<NumberInterval> args) {
        return NumberInterval.UNKNOWN;
    }

    @Nullable
    default NumberDistribution distribution(List<NumberDistribution> args) {
        return null;
    }

    default boolean isSimple(List<NumberExpr> args) {
        return false;
    }

    @Nullable
    default NumberText format(List<NumberExpr> args, List<NumberText> formatted) {
        return null;
    }

    @NotNull
    static String translationKey(Identifier id) {
        return id.getNamespace() + ".number.fn." + id.getPath();
    }

    @NotNull
    static Builder builder(Identifier id) {
        return new Builder(id);
    }

    final class Builder {
        private final Identifier id;
        private int minArity = 0;
        private int maxArity = Integer.MAX_VALUE;
        @Nullable
        private Infix infix;
        private Unit[] units = {Unit.SAME};
        @Nullable
        private Function<List<NumberExpr>, NumberExpr> simplify;
        @Nullable
        private Function<List<NumberExpr>, NumberExpr> expand;
        @Nullable
        private ToDoubleFunction<double[]> evaluate;
        @Nullable
        private Function<List<NumberInterval>, NumberInterval> bounds;
        @Nullable
        private Function<List<NumberDistribution>, NumberDistribution> distribution;
        private Predicate<List<NumberExpr>> simple = (args) -> false;
        @Nullable
        private BiFunction<List<NumberExpr>, List<NumberText>, NumberText> format;

        private Builder(Identifier id) {
            this.id = Objects.requireNonNull(id);
        }

        @NotNull
        public Builder arity(int arity) {
            return arity(arity, arity);
        }

        @NotNull
        public Builder arity(int min, int max) {
            minArity = min;
            maxArity = max;
            return this;
        }

        @NotNull
        public Builder infix(String symbol, int precedence, InfixKind kind) {
            infix = new Infix(symbol, precedence, kind);
            return this;
        }

        @NotNull
        public Builder units(Unit... units) {
            this.units = units.clone();
            return this;
        }

        @NotNull
        public Builder simplify(Function<List<NumberExpr>, NumberExpr> simplify) {
            this.simplify = simplify;
            return this;
        }

        @NotNull
        public Builder expand(Function<List<NumberExpr>, NumberExpr> expand) {
            this.expand = expand;
            return this;
        }

        @NotNull
        public Builder evaluate(ToDoubleFunction<double[]> evaluate) {
            this.evaluate = evaluate;
            return this;
        }

        @NotNull
        public Builder bounds(Function<List<NumberInterval>, NumberInterval> bounds) {
            this.bounds = bounds;
            return this;
        }

        @NotNull
        public Builder distribution(Function<List<NumberDistribution>, NumberDistribution> distribution) {
            this.distribution = distribution;
            return this;
        }

        @NotNull
        public Builder distributionOfParameters(Function<double[], NumberDistribution> family) {
            return distribution((args) -> NumberDistribution.ofParameters(args, family));
        }

        @NotNull
        public Builder simple(Predicate<List<NumberExpr>> simple) {
            this.simple = simple;
            return this;
        }

        @NotNull
        public Builder format(BiFunction<List<NumberExpr>, List<NumberText>, NumberText> format) {
            this.format = format;
            return this;
        }

        @NotNull
        public NumberFunction build() {
            return new Built(this);
        }
    }

    final class Built implements NumberFunction {
        private final Builder b;

        private Built(Builder builder) {
            b = builder;
        }

        @NotNull
        @Override
        public Identifier id() {
            return b.id;
        }

        @Override
        public int minArity() {
            return b.minArity;
        }

        @Override
        public int maxArity() {
            return b.maxArity;
        }

        @Nullable
        @Override
        public Infix infix() {
            return b.infix;
        }

        @NotNull
        @Override
        public Unit unit(int argument) {
            return b.units[Math.min(argument, b.units.length - 1)];
        }

        @Nullable
        @Override
        public NumberExpr simplify(List<NumberExpr> args) {
            return b.simplify != null ? b.simplify.apply(args) : null;
        }

        @Nullable
        @Override
        public NumberExpr expand(List<NumberExpr> args) {
            return b.expand != null ? b.expand.apply(args) : null;
        }

        @NotNull
        @Override
        public OptionalDouble evaluate(double[] args) {
            return b.evaluate != null ? OptionalDouble.of(b.evaluate.applyAsDouble(args)) : OptionalDouble.empty();
        }

        @NotNull
        @Override
        public NumberInterval bounds(List<NumberInterval> args) {
            return b.bounds != null ? b.bounds.apply(args) : NumberInterval.UNKNOWN;
        }

        @Nullable
        @Override
        public NumberDistribution distribution(List<NumberDistribution> args) {
            return b.distribution != null ? b.distribution.apply(args) : null;
        }

        @Override
        public boolean isSimple(List<NumberExpr> args) {
            return b.simple.test(args);
        }

        @Nullable
        @Override
        public NumberText format(List<NumberExpr> args, List<NumberText> formatted) {
            return b.format != null ? b.format.apply(args, formatted) : null;
        }
    }
}
