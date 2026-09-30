package com.yanny.aci.number;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.Utils;
import com.yanny.aci.api.*;
import com.yanny.aci.language.CoreLang;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class NumberFormatter {
    private static final int LOWEST = 0;
    private static final int ATOM = 4;

    private NumberFormatter() {
    }

    public record Formatted(NumberText value, @Nullable NumberText mode) {
    }

    @NotNull
    public static Formatted format(NumberExpr expr, boolean showFormula, boolean percent) {
        return format(expr, showFormula, percent, null);
    }

    @NotNull
    public static Formatted format(NumberExpr expr, boolean showFormula, boolean percent, @Nullable NumberInterval limit) {
        NumberExpr limited = expr;

        if (limit != null) {
            limited = NumberExpr.clamp(expr, NumberExpr.constant(limit.lo()), NumberExpr.constant(limit.hi()));
        }

        NumberText value = showFormula && !expr.isSimple() ? formula(expr, percent).text : text(limited, expr, percent);
        NumberText mode = limited.mode().map((m) -> mode(m, percent)).orElse(null);

        return new Formatted(value, mode);
    }

    @NotNull
    public static NumberText text(NumberExpr expr, boolean percent) {
        return text(expr, expr, percent);
    }

    @NotNull
    public static NumberInterval slotBounds(NumberExpr expr) {
        try {
            return expr.bounds();
        } catch (Throwable e) {
            CommonLogUtils.getLogger(Utils.MOD_ID).warn("Failed to compute bounds of {}: {}", expr, e.getMessage(), e);
            return NumberInterval.UNKNOWN;
        }
    }

    @NotNull
    public static String slot(NumberInterval interval) {
        if (interval.isUnknown() || Double.isInfinite(interval.lo()) || Double.isInfinite(interval.hi())) {
            return "?";
        }

        long lo = (long) Math.ceil(interval.lo());
        long hi = (long) Math.floor(interval.hi());

        if (!interval.loClosed() && lo == interval.lo()) {
            lo++;
        }
        if (!interval.hiClosed() && hi == interval.hi()) {
            hi--;
        }

        return lo >= hi ? Long.toString(lo) : lo + "-" + hi;
    }

    @NotNull
    public static NumberText interval(NumberInterval interval, boolean percent) {
        if (interval.isUnknown()) {
            return key(CoreLang.Numbers.UNKNOWN);
        }
        if (interval.isPoint()) {
            return number(interval.lo(), percent);
        }
        if (Double.isInfinite(interval.lo()) && Double.isInfinite(interval.hi())) {
            return key(CoreLang.Numbers.ANY);
        }
        if (Double.isInfinite(interval.lo())) {
            return key(CoreLang.Numbers.AT_MOST, number(interval.hi(), percent));
        }
        if (Double.isInfinite(interval.hi())) {
            return key(CoreLang.Numbers.AT_LEAST, number(interval.lo(), percent));
        }

        return key(CoreLang.Numbers.RANGE, number(interval.lo(), percent), number(interval.hi(), percent));
    }

    @NotNull
    public static NumberText mode(NumberMode mode, boolean percent) {
        NumberText value;

        if (mode.lo() == mode.hi()) {
            value = number(mode.lo(), percent);
        } else {
            value = key(CoreLang.Numbers.RANGE, number(mode.lo(), percent), number(mode.hi(), percent));
        }

        if (mode.hasProbability()) {
            return key(CoreLang.Numbers.MODE, value, key(CoreLang.Numbers.PERCENT, NumberText.num(Math.round(mode.probability() * 100))));
        }

        return key(CoreLang.Numbers.MODE_PEAK, value);
    }

    @NotNull
    public static NumberText number(double value, boolean percent) {
        return percent ? key(CoreLang.Numbers.PERCENT, NumberText.num(value * 100)) : NumberText.num(value);
    }

    @NotNull
    public static String varKey(ResourceLocation type) {
        return type.getNamespace() + ".number.var." + type.getPath();
    }

    @NotNull
    private static NumberText text(NumberExpr limited, NumberExpr expr, boolean percent) {
        NumberText range = interval(limited.bounds(), percent);
        List<NumberText> labels = labels(expr);

        if (labels.isEmpty()) {
            return range;
        }

        return NumberText.seq(range, NumberText.str(" "), key(CoreLang.Numbers.DEPENDS, join(labels, ", ")));
    }

    @NotNull
    private static List<NumberText> labels(NumberExpr expr) {
        Set<NumberText> labels = new LinkedHashSet<>();

        expr.transform((e) -> {
            if (e instanceof NumberExpr.Var v && !v.type().equals(NumberExpr.LEVEL)) {
                labels.add(new NumberText.Key(varKey(v.type()) + ".desc", v.args()));
            } else if (e instanceof NumberExpr.Opaque o) {
                labels.add(NumberText.str(o.typeId()));
            } else if (e instanceof NumberExpr.Fn f && f.function() == null) {
                labels.add(NumberText.str(f.id().toString()));
            }

            return e;
        });
        return new ArrayList<>(labels);
    }

    @NotNull
    private static Part formula(NumberExpr expr, boolean percent) {
        if (expr instanceof NumberExpr.Const c) {
            return new Part(number(c.value(), percent), c.value() < 0 ? NumberFunction.UNARY : ATOM);
        } else if (expr instanceof NumberExpr.Range r) {
            return new Part(range(r, percent), ATOM);
        } else if (expr instanceof NumberExpr.Var v) {
            return new Part(new NumberText.Key(varKey(v.type()), v.args()), ATOM);
        } else if (expr instanceof NumberExpr.Lookup l) {
            return new Part(lookup(l, percent), ATOM);
        } else if (expr instanceof NumberExpr.Fn f) {
            return function(f, percent);
        } else if (expr instanceof NumberExpr.Cond c) {
            List<NumberText> parts = new ArrayList<>();

            c.branches().forEach((b) -> parts.add(formula(b.value(), percent).text));
            parts.add(formula(c.otherwise(), percent).text);
            return new Part(join(parts, " | "), LOWEST);
        } else if (expr instanceof NumberExpr.Weighted w) {
            List<NumberText> parts = new ArrayList<>();
            double total = w.totalWeight();

            w.entries().forEach((e) -> parts.add(NumberText.seq(
                    formula(e.value(), percent).text,
                    NumberText.str(" ("),
                    number(e.weight() / total, true),
                    NumberText.str(")")
            )));
            return new Part(join(parts, " | "), LOWEST);
        } else if (expr instanceof NumberExpr.Opaque o) {
            return new Part(unknown(o.typeId()), ATOM);
        }

        throw new IllegalStateException("Unhandled number expression " + expr);
    }

    @NotNull
    private static NumberText unknown(String id) {
        return NumberText.seq(key(CoreLang.Numbers.UNKNOWN), NumberText.str(" "), key(CoreLang.Numbers.DEPENDS, NumberText.str(id)));
    }

    @NotNull
    private static NumberText range(NumberExpr.Range r, boolean percent) {
        if (r.min() == null && r.max() == null) {
            return key(CoreLang.Numbers.ANY);
        }
        if (r.min() == null) {
            return key(CoreLang.Numbers.AT_MOST, formula(r.max(), percent).text);
        }
        if (r.max() == null) {
            return key(CoreLang.Numbers.AT_LEAST, formula(r.min(), percent).text);
        }

        return NumberText.seq(
                NumberText.str(r.minClosed() ? "[" : "("),
                formula(r.min(), percent).text,
                NumberText.str("; "),
                formula(r.max(), percent).text,
                NumberText.str(r.maxClosed() ? "]" : ")")
        );
    }

    @NotNull
    private static NumberText lookup(NumberExpr.Lookup l, boolean percent) {
        List<NumberText> values = l.values().stream().map((v) -> formula(v, percent).text).toList();
        NumberExpr index = l.index();

        if (l.fallback() == null) {
            index = NumberExpr.min(index, NumberExpr.constant(l.values().size() - 1));
        }

        NumberText table = NumberText.seq(NumberText.str("["), join(values, "; "), NumberText.str("]"));
        NumberText selector = NumberText.seq(NumberText.str("["), formula(index, false).text, NumberText.str("]"));

        if (l.fallback() == null) {
            return NumberText.seq(table, selector);
        }

        return NumberText.seq(table, selector, NumberText.str(" | "), formula(l.fallback(), percent).text);
    }

    @NotNull
    private static Part function(NumberExpr.Fn f, boolean percent) {
        NumberFunction function = f.function();

        if (function == null) {
            return new Part(unknown(f.id().toString()), ATOM);
        }

        List<NumberExpr> args = f.args();
        NumberFunction.Infix infix = function.infix();

        if (infix != null && infix.kind() == NumberFunction.InfixKind.PREFIX && args.size() == 1) {
            return new Part(NumberText.seq(NumberText.str(infix.symbol()), operand(args.get(0), argPercent(function, 0, percent), infix.precedence())), infix.precedence());
        }
        if (infix != null && infix.kind() != NumberFunction.InfixKind.PREFIX) {
            List<NumberText> parts = new ArrayList<>();

            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    parts.add(NumberText.str(" " + infix.symbol() + " "));
                }

                boolean strict = infix.kind() == NumberFunction.InfixKind.LEFT && i > 0;

                parts.add(operand(args.get(i), argPercent(function, i, percent), strict ? infix.precedence() + 1 : infix.precedence()));
            }

            return new Part(NumberText.seq(parts), infix.precedence());
        }

        List<NumberText> formatted = new ArrayList<>();

        for (int i = 0; i < args.size(); i++) {
            formatted.add(formula(args.get(i), argPercent(function, i, percent)).text);
        }

        NumberText custom = function.format(args, formatted);

        return new Part(Objects.requireNonNullElseGet(custom, () -> NumberText.seq(
                NumberText.key(NumberFunction.translationKey(f.id())),
                NumberText.str("("),
                join(formatted, "; "),
                NumberText.str(")")
        )), ATOM);

    }

    private static boolean argPercent(NumberFunction function, int index, boolean percent) {
        return switch (function.unit(index)) {
            case SAME -> percent;
            case PLAIN -> false;
            case PERCENT -> true;
        };
    }

    @NotNull
    private static NumberText operand(NumberExpr expr, boolean percent, int required) {
        Part part = formula(expr, percent);

        if (part.precedence >= required) {
            return part.text;
        }

        return NumberText.seq(NumberText.str("("), part.text, NumberText.str(")"));
    }

    @NotNull
    private static NumberText join(List<NumberText> parts, String separator) {
        List<NumberText> joined = new ArrayList<>();

        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                joined.add(NumberText.str(separator));
            }

            joined.add(parts.get(i));
        }

        return NumberText.seq(joined);
    }

    @NotNull
    private static NumberText key(CoreLang.Numbers key, NumberText... args) {
        return NumberText.key(key.singular(), args);
    }

    private record Part(NumberText text, int precedence) {
    }
}
