package com.yanny.aci.number;

import com.yanny.aci.api.NumberDistribution;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunction;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.api.NumberText;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

import static com.yanny.aci.api.NumberFunction.InfixKind.*;
import static com.yanny.aci.api.NumberFunction.Unit.*;
import static com.yanny.aci.api.NumberFunctions.*;

public final class BuiltinFunctions {
    private static final NumberInterval UNIT = NumberInterval.closed(-1, 1);

    private BuiltinFunctions() {
    }

    public static void registerAll() {
        register(NumberFunction.builder(ADD).arity(1, Integer.MAX_VALUE).infix("+", NumberFunction.ADDITIVE, ASSOCIATIVE)
                .evaluate((v) -> Arrays.stream(v).sum())
                .simplify(BuiltinFunctions::simplifyAdd)
                .bounds((a) -> reduce(a, NumberInterval::plus))
                .distribution((a) -> reduceNullable(a, NumberDistribution::plus)));
        register(NumberFunction.builder(SUB).arity(2).infix("−", NumberFunction.ADDITIVE, LEFT)
                .evaluate((v) -> v[0] - v[1])
                .simplify((a) -> simplifySub(a.get(0), a.get(1)))
                .bounds((a) -> a.get(0).minus(a.get(1)))
                .distribution((a) -> a.get(0).plus(a.get(1).affine(-1, 0))));
        register(NumberFunction.builder(MUL).arity(1, Integer.MAX_VALUE).infix("×", NumberFunction.MULTIPLICATIVE, ASSOCIATIVE)
                .evaluate((v) -> Arrays.stream(v).reduce(1, (x, y) -> x * y))
                .simplify(BuiltinFunctions::simplifyMul)
                .bounds((a) -> reduce(a, NumberInterval::times))
                .distribution((a) -> reduceNullable(a, NumberDistribution::times)));
        register(NumberFunction.builder(DIV).arity(2).infix("÷", NumberFunction.MULTIPLICATIVE, LEFT)
                .evaluate((v) -> v[0] / v[1])
                .bounds((a) -> a.get(0).dividedBy(a.get(1)))
                .distribution((a) -> a.get(0).dividedBy(a.get(1))));
        register(NumberFunction.builder(NEG).arity(1).infix("−", NumberFunction.UNARY, PREFIX)
                .evaluate((v) -> -v[0])
                .bounds((a) -> a.get(0).negate())
                .distribution((a) -> a.get(0).affine(-1, 0)));
        register(binary(MOD, (x, y) -> x % y, NumberInterval::mod));
        register(binary(FLOOR_MOD, (x, y) -> x - Math.floor(x / y) * y, NumberInterval::floorMod));
        register(binary(FLOOR_DIV, (x, y) -> Math.floor(x / y), (x, y) -> x.dividedBy(y).floor()));
        register(NumberFunction.builder(POW).arity(2).units(SAME, PLAIN)
                .evaluate((v) -> Math.pow(v[0], v[1]))
                .bounds((a) -> a.get(0).pow(a.get(1)))
                .distribution((a) -> a.get(0).combine(a.get(1), Math::pow)));
        register(NumberFunction.builder(MIN).arity(1, Integer.MAX_VALUE)
                .evaluate((v) -> Arrays.stream(v).min().orElseThrow())
                .bounds((a) -> reduce(a, NumberInterval::min))
                .distribution((a) -> reduceNullable(a, NumberDistribution::min)));
        register(NumberFunction.builder(MAX).arity(1, Integer.MAX_VALUE)
                .evaluate((v) -> Arrays.stream(v).max().orElseThrow())
                .bounds((a) -> reduce(a, NumberInterval::max))
                .distribution((a) -> reduceNullable(a, NumberDistribution::max)));
        register(unary(ABS, Math::abs, NumberInterval::abs).distribution((a) -> a.get(0).abs()));
        register(rounding(FLOOR, NumberDistribution.Rounding.FLOOR, NumberInterval::floor));
        register(rounding(CEIL, NumberDistribution.Rounding.CEIL, NumberInterval::ceil));
        register(rounding(ROUND, NumberDistribution.Rounding.ROUND, NumberInterval::round));
        register(rounding(TRUNC, NumberDistribution.Rounding.TRUNC, NumberInterval::trunc));
        register(unary(SQRT, Math::sqrt, NumberInterval::sqrt).distribution((a) -> a.get(0).map(Math::sqrt)));
        register(unary(SIN, Math::sin, (a) -> UNIT).distribution((a) -> a.get(0).map(Math::sin)));
        register(unary(COS, Math::cos, (a) -> UNIT).distribution((a) -> a.get(0).map(Math::cos)));
        register(NumberFunction.builder(LENGTH).arity(1, Integer.MAX_VALUE)
                .evaluate((v) -> Math.sqrt(Arrays.stream(v).map((x) -> x * x).sum()))
                .bounds((a) -> reduce(a.stream().map(NumberInterval::square).toList(), NumberInterval::plus).sqrt())
                .distribution((a) -> {
                    NumberDistribution squares = reduceNullable(a.stream().map((d) -> d.map((x) -> x * x)).toList(), NumberDistribution::plus);

                    return squares != null ? squares.map(Math::sqrt) : null;
                }));
        register(NumberFunction.builder(CLAMP).arity(3)
                .evaluate((v) -> Math.min(Math.max(v[0], v[1]), v[2]))
                .bounds((a) -> a.get(0).max(a.get(1)).min(a.get(2)))
                .distribution((a) -> a.get(0).clamp(a.get(1), a.get(2))));
        register(NumberFunction.builder(AVG).arity(1, Integer.MAX_VALUE)
                .evaluate((v) -> Arrays.stream(v).average().orElseThrow())
                .bounds((a) -> reduce(a, NumberInterval::plus).times(NumberInterval.point(1.0 / a.size())))
                .distribution((a) -> {
                    NumberDistribution total = reduceNullable(a, NumberDistribution::plus);

                    return total != null ? total.affine(1.0 / a.size(), 0) : null;
                }));
        register(NumberFunction.builder(UNIFORM_INT).arity(2)
                .simplify((a) -> a.get(0).equals(a.get(1)) ? a.get(0) : null)
                .simple((a) -> a.stream().allMatch(NumberExpr.Const.class::isInstance))
                .bounds((a) -> NumberInterval.closed(a.get(0).lo(), a.get(1).hi()))
                .distributionOfParameters((p) -> NumberDistribution.uniformInt((long) p[0], (long) p[1]))
                .format((a, f) -> interval(UNIFORM_INT, f, true)));
        register(NumberFunction.builder(UNIFORM_FLOAT).arity(2)
                .simplify((a) -> a.get(0).equals(a.get(1)) ? a.get(0) : null)
                .bounds((a) -> new NumberInterval(a.get(0).lo(), a.get(1).hi(), a.get(0).loClosed(), false))
                .distributionOfParameters((p) -> NumberDistribution.uniformFloat(p[0], p[1]))
                .format((a, f) -> interval(UNIFORM_FLOAT, f, false)));
        register(NumberFunction.builder(BINOMIAL).arity(2).units(PLAIN, PERCENT)
                .bounds((a) -> NumberInterval.closed(0, Math.max(0, Math.floor(a.get(0).hi()))))
                .distributionOfParameters((p) -> binomial((long) p[0], p[1])));
        register(NumberFunction.builder(NORMAL).arity(2)
                .bounds((a) -> NumberInterval.ALL)
                .distributionOfParameters((p) -> normal(p[0], p[1])));
        register(NumberFunction.builder(BIASED_TO_BOTTOM).arity(2)
                .expand((a) -> NumberExpr.add(a.get(0), NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.sub(a.get(1), a.get(0)))))));
        register(NumberFunction.builder(VERY_BIASED_TO_BOTTOM).arity(2)
                .expand((a) -> NumberExpr.add(a.get(0), NumberExpr.uniformInt(NumberExpr.constant(0),
                        NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.sub(a.get(1), a.get(0))))))));
        register(NumberFunction.builder(TRAPEZOID_FLOAT).arity(3)
                .bounds((a) -> new NumberInterval(a.get(0).lo(), a.get(1).hi(), a.get(0).loClosed(), false))
                .distributionOfParameters((p) -> trapezoidFloat(p[0], p[1], p[2])));
        register(NumberFunction.builder(TRAPEZOID_INT).arity(3)
                .expand(BuiltinFunctions::expandTrapezoidInt));
    }

    @Nullable
    private static NumberExpr expandTrapezoidInt(List<NumberExpr> args) {
        if (!(args.get(0) instanceof NumberExpr.Const(double min) && args.get(1) instanceof NumberExpr.Const(double max)
                && args.get(2) instanceof NumberExpr.Const(double plateau))) {
            return null;
        }

        if (plateau == 0 && max == -min) {
            return NumberExpr.sub(NumberExpr.uniformInt(0, max), NumberExpr.uniformInt(0, max));
        }

        long range = (long) (max - min);
        long rest = (range - (long) plateau) / 2;

        return NumberExpr.add(NumberExpr.constant(min), NumberExpr.uniformInt(0, range - rest), NumberExpr.uniformInt(0, rest));
    }

    @NotNull
    private static NumberFunction.Builder unary(Identifier id, DoubleUnaryOperator f, UnaryOperator<NumberInterval> bounds) {
        return NumberFunction.builder(id).arity(1)
                .evaluate((v) -> f.applyAsDouble(v[0]))
                .bounds((a) -> bounds.apply(a.get(0)));
    }

    @NotNull
    private static NumberFunction.Builder rounding(Identifier id, NumberDistribution.Rounding rounding, UnaryOperator<NumberInterval> bounds) {
        return unary(id, rounding::apply, bounds).distribution((a) -> a.get(0).round(rounding));
    }

    @NotNull
    private static NumberFunction.Builder binary(Identifier id, DoubleBinaryOperator f, BinaryOperator<NumberInterval> bounds) {
        return NumberFunction.builder(id).arity(2)
                .evaluate((v) -> f.applyAsDouble(v[0], v[1]))
                .bounds((a) -> bounds.apply(a.get(0), a.get(1)))
                .distribution((a) -> a.get(0).combine(a.get(1), f));
    }

    private static void register(NumberFunction.Builder builder) {
        NumberFunctions.register(builder.build());
    }

    @NotNull
    private static NumberInterval reduce(List<NumberInterval> args, BinaryOperator<NumberInterval> op) {
        NumberInterval result = args.get(0);

        for (int i = 1; i < args.size(); i++) {
            result = op.apply(result, args.get(i));
        }

        return result;
    }

    @Nullable
    private static NumberDistribution reduceNullable(List<NumberDistribution> args, BinaryOperator<NumberDistribution> op) {
        NumberDistribution result = args.get(0);

        for (int i = 1; i < args.size() && result != null; i++) {
            NumberDistribution next = args.get(i);

            result = next != null ? op.apply(result, next) : null;
        }

        return result;
    }

    @NotNull
    private static NumberText interval(Identifier id, List<NumberText> formatted, boolean maxClosed) {
        return NumberText.seq(
                NumberText.key(NumberFunction.translationKey(id)),
                NumberText.str("["),
                formatted.get(0),
                NumberText.str("; "),
                formatted.get(1),
                NumberText.str(maxClosed ? "]" : ")")
        );
    }

    @NotNull
    private static NumberExpr simplifyAdd(List<NumberExpr> args) {
        List<NumberExpr> terms = new ArrayList<>();
        double constant = 0;
        int constantIndex = -1;

        for (NumberExpr arg : args) {
            if (arg instanceof NumberExpr.Const c) {
                constant += c.value();
                constantIndex = constantIndex < 0 ? terms.size() : constantIndex;
            } else if (arg instanceof NumberExpr.Fn f && f.id().equals(ADD)) {
                terms.addAll(f.args());
            } else {
                terms.add(arg);
            }
        }

        if (constant < 0 && !terms.isEmpty()) {
            return simplifySub(terms.size() == 1 ? terms.get(0) : new NumberExpr.Fn(ADD, terms), NumberExpr.constant(-constant));
        }
        if (constant != 0 || terms.isEmpty()) {
            terms.add(Math.max(constantIndex, 0), NumberExpr.constant(constant));
        }

        return terms.size() == 1 ? terms.get(0) : new NumberExpr.Fn(ADD, terms);
    }

    @NotNull
    private static NumberExpr simplifySub(NumberExpr a, NumberExpr b) {
        if (b instanceof NumberExpr.Const c && c.value() == 0) {
            return a;
        }
        if (b instanceof NumberExpr.Const c && c.value() < 0) {
            return simplifyAdd(List.of(a, NumberExpr.constant(-c.value())));
        }

        return new NumberExpr.Fn(SUB, List.of(a, b));
    }

    @NotNull
    private static NumberExpr simplifyMul(List<NumberExpr> args) {
        List<NumberExpr> factors = new ArrayList<>();
        double constant = 1;
        int constantIndex = -1;

        for (NumberExpr arg : args) {
            if (arg instanceof NumberExpr.Const c) {
                constant *= c.value();
                constantIndex = constantIndex < 0 ? factors.size() : constantIndex;
            } else if (arg instanceof NumberExpr.Fn f && f.id().equals(MUL)) {
                factors.addAll(f.args());
            } else {
                factors.add(arg);
            }
        }

        if (constant == 0) {
            return NumberExpr.constant(0);
        }
        if (constant != 1 || factors.isEmpty()) {
            factors.add(Math.max(constantIndex, 0), NumberExpr.constant(constant));
        }

        return factors.size() == 1 ? factors.get(0) : new NumberExpr.Fn(MUL, factors);
    }

    @Nullable
    private static NumberDistribution binomial(long n, double p) {
        if (n <= 0 || p <= 0) {
            return NumberDistribution.constant(0);
        }
        if (p >= 1) {
            return NumberDistribution.constant(n);
        }
        if (n + 1 > NumberDistribution.MAX_ATOMS) {
            return null;
        }

        Map<Double, Double> atoms = new TreeMap<>();
        double logChoose = 0;

        for (long k = 0; k <= n; k++) {
            atoms.put((double) k, Math.exp(logChoose + k * Math.log(p) + (n - k) * Math.log1p(-p)));
            logChoose += Math.log(n - k) - Math.log(k + 1);
        }

        return NumberDistribution.discrete(atoms);
    }

    @NotNull
    private static NumberDistribution normal(double mean, double deviation) {
        if (deviation <= 0) {
            return NumberDistribution.constant(mean);
        }

        DoubleUnaryOperator cdf = (x) -> normalCdf((x - mean) / deviation);
        double density = 1 / (deviation * Math.sqrt(2 * Math.PI));

        return NumberDistribution.continuous(new NumberDistribution.Density(cdf, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, mean, mean, density, false));
    }

    @Nullable
    private static NumberDistribution trapezoidFloat(double min, double max, double plateau) {
        double g = (max - min - plateau) / 2;
        double h = max - min - g;
        double a = Math.min(g, h);
        double b = Math.max(g, h);

        if (a < 0) {
            return null;
        }
        if (a == 0) {
            return NumberDistribution.uniformFloat(min, min + b);
        }

        DoubleUnaryOperator cdf = (x) -> {
            double s = x - min;

            if (s <= 0) {
                return 0;
            } else if (s < a) {
                return s * s / (2 * a * b);
            } else if (s < b) {
                return (s - a / 2) / b;
            } else if (s < a + b) {
                return 1 - (a + b - s) * (a + b - s) / (2 * a * b);
            }

            return 1;
        };

        return NumberDistribution.continuous(new NumberDistribution.Density(cdf, min, min + a + b, 1, min + a, min + b, 1 / b, false));
    }

    private static double normalCdf(double z) {
        return z < 0 ? 0.5 * erfc(-z / Math.sqrt(2)) : 1 - 0.5 * erfc(z / Math.sqrt(2));
    }

    // Constants are Numerical Recipes' erfcc (fractional error < 1.2e-7).
    private static double erfc(double x) {
        double t = 1 / (1 + 0.5 * x);
        double poly = -x * x - 1.26551223 + t * (1.00002368 + t * (0.37409196 + t * (0.09678418 + t * (-0.18628806
                + t * (0.27886807 + t * (-1.13520398 + t * (1.48851587 + t * (-0.82215223 + t * 0.17087277))))))));

        return t * Math.exp(poly);
    }
}
