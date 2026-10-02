package com.yanny.aci.number;

import com.yanny.aci.api.NumberDistribution;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunction;
import com.yanny.aci.api.NumberInterval;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NumberEvaluator {
    private NumberEvaluator() {
    }

    @NotNull
    public static NumberInterval bounds(NumberExpr expr) {
        if (expr instanceof NumberExpr.Const c) {
            return NumberInterval.point(c.value());
        } else if (expr instanceof NumberExpr.Range r) {
            return range(r);
        } else if (expr instanceof NumberExpr.Var v) {
            return new NumberInterval(v.min(), v.max(), true, true);
        } else if (expr instanceof NumberExpr.Lookup l) {
            NumberInterval result = l.fallback() != null ? bounds(l.fallback()) : bounds(l.values().get(0));

            for (NumberExpr value : l.values()) {
                result = result.union(bounds(value));
            }

            return result;
        } else if (expr instanceof NumberExpr.Fn f) {
            return function(f);
        } else if (expr instanceof NumberExpr.Cond c) {
            NumberInterval result = bounds(c.otherwise());

            for (NumberExpr.Branch branch : c.branches()) {
                result = result.union(bounds(branch.value()));
            }

            return result;
        } else if (expr instanceof NumberExpr.Weighted w) {
            NumberInterval result = bounds(w.entries().get(0).value());

            for (NumberExpr.WeightedEntry entry : w.entries()) {
                result = result.union(bounds(entry.value()));
            }

            return result;
        }

        return NumberInterval.UNKNOWN;
    }

    @Nullable
    public static NumberDistribution distribution(NumberExpr expr) {
        if (expr instanceof NumberExpr.Const c) {
            return NumberDistribution.constant(c.value());
        } else if (expr instanceof NumberExpr.Lookup l) {
            return lookup(l);
        } else if (expr instanceof NumberExpr.Fn f) {
            return functionDistribution(f);
        } else if (expr instanceof NumberExpr.Weighted w) {
            double total = w.totalWeight();
            List<NumberDistribution.Weight> parts = new ArrayList<>();

            for (NumberExpr.WeightedEntry entry : w.entries()) {
                parts.add(new NumberDistribution.Weight(entry.weight() / total, distribution(entry.value())));
            }

            return NumberDistribution.mixture(parts);
        }

        return null;
    }

    @NotNull
    private static NumberInterval range(NumberExpr.Range r) {
        double lo = Double.NEGATIVE_INFINITY;
        double hi = Double.POSITIVE_INFINITY;
        boolean loClosed = false;
        boolean hiClosed = false;

        if (r.min() != null) {
            NumberInterval min = bounds(r.min());

            lo = min.lo();
            loClosed = r.minClosed() && min.loClosed();
        }
        if (r.max() != null) {
            NumberInterval max = bounds(r.max());

            hi = max.hi();
            hiClosed = r.maxClosed() && max.hiClosed();
        }

        return new NumberInterval(lo, hi, loClosed, hiClosed);
    }

    @NotNull
    private static NumberInterval function(NumberExpr.Fn f) {
        NumberFunction function = f.function();

        if (function == null) {
            return NumberInterval.UNKNOWN;
        }

        NumberExpr expanded = function.expand(f.args());

        if (expanded != null) {
            return bounds(expanded);
        }

        List<NumberInterval> args = f.args().stream().map(NumberEvaluator::bounds).toList();

        if (args.stream().anyMatch(NumberInterval::isUnknown)) {
            return NumberInterval.UNKNOWN;
        }

        return function.bounds(args);
    }

    @Nullable
    private static NumberDistribution functionDistribution(NumberExpr.Fn f) {
        NumberFunction function = f.function();

        if (function == null) {
            return null;
        }

        NumberExpr expanded = function.expand(f.args());

        if (expanded != null) {
            return distribution(expanded);
        }

        List<NumberDistribution> args = new ArrayList<>();

        for (NumberExpr arg : f.args()) {
            NumberDistribution distribution = distribution(arg);

            if (distribution == null) {
                return null;
            }

            args.add(distribution);
        }

        return function.distribution(args);
    }

    @Nullable
    private static NumberDistribution lookup(NumberExpr.Lookup l) {
        NumberDistribution index = distribution(l.index());

        if (index == null || !index.isDiscrete()) {
            return null;
        }

        List<NumberDistribution.Weight> parts = new ArrayList<>();

        for (Map.Entry<Double, Double> entry : index.atoms().entrySet()) {
            int i = (int) Math.floor(entry.getKey());
            NumberExpr value;

            if (i >= 0 && i < l.values().size()) {
                value = l.values().get(i);
            } else {
                value = l.fallback() != null ? l.fallback() : l.values().get(l.values().size() - 1);
            }

            parts.add(new NumberDistribution.Weight(entry.getValue(), distribution(value)));
        }

        return NumberDistribution.mixture(parts);
    }
}
