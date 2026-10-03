package com.yanny.aci.tooltip;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.Utils;
import com.yanny.aci.api.NumberDistribution;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.api.NumberMode;
import com.yanny.aci.api.NumberText;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.number.NumberFormatter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BiFunction;

final class NumberView {
    private static final int NO_COLOR = -1;
    private static final int MAX_COLUMNS = 64;
    private static final int MAX_LEVEL_ROWS = 255;
    private static final double TAIL = 1e-3;
    private static final double TIE = 1e-6;

    @Nullable
    private final Row main;
    private final List<Row> rows;
    @Nullable
    private final ChartData chart;
    private final boolean error;

    private NumberView(@Nullable Row main, List<Row> rows, @Nullable ChartData chart, boolean error) {
        this.main = main;
        this.rows = rows;
        this.chart = chart;
        this.error = error;
    }

    private record Row(int depth, @Nullable NumberText name, @Nullable NumberText label, @Nullable NumberText value, @Nullable NumberText mode,
                       int condition, int color) {
    }

    private record ChartData(List<SeriesData> series, int columnWidth) {
    }

    private record SeriesData(int level, List<Double> heights, List<Boolean> modes, int first, int last, NumberText min, NumberText max) {
    }

    private record Column(double lo, double hi, boolean closed) {
        boolean intersects(double from, double to, boolean last) {
            if (closed) {
                return from <= hi && to >= lo;
            }

            return to >= lo && (from < hi || (last && from <= hi));
        }

        double size() {
            return closed ? hi - lo + 1 : 1;
        }
    }

    @NotNull
    static NumberView compute(TooltipNumber number, boolean showFormulas, boolean showCharts) {
        try {
            return build(number, showFormulas, showCharts);
        } catch (Throwable e) {
            CommonLogUtils.getLogger(Utils.MOD_ID).warn("Failed to render number {}: {}", number.expr(), e.getMessage(), e);
            return new NumberView(null, List.of(), null, true);
        }
    }

    @NotNull
    Component value(TooltipStyle style, Locale locale) {
        if (error) {
            return NumberText.key(CoreLang.Numbers.UNKNOWN.singular()).toComponent(locale).withStyle(style.error());
        }
        if (main == null) {
            return Component.empty();
        }

        return value(main, style, locale);
    }

    @NotNull
    List<TooltipLine> lines(int indent, TooltipStyle style, Locale locale, BiFunction<Integer, Integer, List<TooltipLine>> condition) {
        List<TooltipLine> lines = new ArrayList<>();

        for (Row row : rows) {
            MutableComponent line = Component.literal("  ".repeat(indent + row.depth));

            line.append(Component.literal("-> ").withStyle(style.branch()));

            if (row.name != null) {
                Style labelStyle = row.color != NO_COLOR ? Style.EMPTY.withColor(style.level(row.color)) : style.text();
                Component name = row.name.toComponent(locale).withStyle(labelStyle);

                if (row.label != null) {
                    Component level = row.label.toComponent(locale).withStyle(labelStyle);

                    line.append(Component.translatable(CoreLang.Numbers.LEVEL_ROW.singular(), name, level, value(row, style, locale)).withStyle(style.text()));
                } else {
                    line.append(Component.translatable(CoreLang.Numbers.VAR_ROW.singular(), name, value(row, style, locale)).withStyle(style.text()));
                }
            } else {
                line.append(value(row, style, locale));
            }

            lines.add(TooltipLine.text(line));

            if (row.condition >= 0) {
                lines.addAll(condition.apply(row.condition, indent + row.depth + 1));
            }
        }

        if (chart != null) {
            List<TooltipLine.Series> series = new ArrayList<>();

            for (SeriesData data : chart.series) {
                series.add(new TooltipLine.Series(
                        data.heights,
                        data.modes,
                        data.level == NO_COLOR ? 0 : style.level(data.level),
                        data.level == NO_COLOR,
                        data.first,
                        data.last,
                        data.min.toComponent(locale).withStyle(style.secondary()),
                        data.max.toComponent(locale).withStyle(style.secondary())
                ));
            }

            lines.add(new TooltipLine.Chart(indent + 1, chart.columnWidth, series));
        }

        return lines;
    }

    boolean isConditionChild(int index) {
        return rows.stream().anyMatch((r) -> r.condition == index);
    }

    @NotNull
    private static Component value(Row row, TooltipStyle style, Locale locale) {
        MutableComponent value = row.value != null ? row.value.toComponent(locale).withStyle(style.value()) : Component.empty();

        if (row.mode != null) {
            value.append(Component.literal("  ")).append(row.mode.toComponent(locale).withStyle(style.secondary()));
        }

        return value;
    }

    @Nullable
    private static NumberText rowLabel(NumberExpr.Var var, int value) {
        if (var.type().equals(NumberExpr.LEVEL)) {
            return NumberText.key(value == 0 ? CoreLang.Numbers.VAR_LEVEL.singular() : "enchantment.level." + value);
        }

        return null;
    }

    @NotNull
    private static NumberView build(TooltipNumber number, boolean showFormulas, boolean showCharts) {
        NumberExpr expr = number.expr();
        List<Row> rows = new ArrayList<>();

        if (expr instanceof NumberExpr.Cond c) {
            for (int i = 0; i < c.branches().size(); i++) {
                NumberExpr.Branch branch = c.branches().get(i);

                block(rows, branch.value(), number, showFormulas, 1, branch.condition(), null, i > 0);
            }

            block(rows, c.otherwise(), number, showFormulas, 1, -1, null, true);
            return new NumberView(null, rows, null, false);
        }
        if (expr instanceof NumberExpr.Weighted w) {
            double total = w.totalWeight();
            NumberFormatter.Formatted formatted = format(unbound(expr), false, number);

            w.entries().forEach((e) -> block(rows, e.value(), number, showFormulas, 1, -1, NumberFormatter.number(e.weight() / total, true), false));

            Row main = new Row(0, null, null, formatted.value(), formatted.mode(), -1, NO_COLOR);

            return new NumberView(main, rows, showCharts ? chart(List.of(unbound(expr)), true, number) : null, false);
        }

        List<NumberExpr> levels = new ArrayList<>();
        NumberFormatter.Formatted formatted = format(unbound(expr), showFormulas, number);

        levelRows(rows, expr, number, showFormulas, 1, showCharts, levels);

        Row main = new Row(0, null, null, formatted.value(), formatted.mode(), -1, NO_COLOR);
        List<NumberExpr> series = new ArrayList<>();
        boolean hasBase = steppedVars(expr).stream().allMatch(NumberView::containsZero);

        if (hasBase) {
            series.add(unbound(expr));
        }

        series.addAll(levels);

        ChartData chart = showCharts ? chart(series, hasBase, number) : null;

        if (chart == null) {
            rows.replaceAll((r) -> new Row(r.depth, r.name, r.label, r.value, r.mode, r.condition, NO_COLOR));
        }

        return new NumberView(main, rows, chart, false);
    }

    private static void block(List<Row> rows, NumberExpr value, TooltipNumber number, boolean showFormulas, int depth, int condition,
                              @Nullable NumberText probability, boolean otherwise) {
        NumberFormatter.Formatted formatted = format(unbound(value), showFormulas, number);
        NumberText text = formatted.value();

        if (probability != null) {
            text = NumberText.key(CoreLang.Numbers.WEIGHTED_ENTRY.singular(), text, probability);
        }
        if (otherwise) {
            text = NumberText.key(CoreLang.Numbers.OTHERWISE.singular(), text);
        }

        rows.add(new Row(depth, null, null, text, formatted.mode(), condition, NO_COLOR));
        levelRows(rows, value, number, showFormulas, depth + 1, false, new ArrayList<>());
    }

    private static void levelRows(List<Row> rows, NumberExpr expr, TooltipNumber number, boolean showFormulas, int depth, boolean colored,
                                  List<NumberExpr> levels) {
        List<NumberExpr.Var> vars = steppedVars(expr);

        for (NumberExpr.Var var : vars) {
            NumberExpr single = expr;

            for (NumberExpr.Var other : vars) {
                if (!other.equals(var) && containsZero(other)) {
                    single = single.bind(other, 0);
                }
            }

            if (showFormulas) {
                rows.add(new Row(depth, rowName(var, 0), rowLabel(var, 0), NumberFormatter.format(single, true, number.percent()).value(), null, -1, NO_COLOR));
            }

            int min = (int) Math.max(Math.ceil(var.min()), -MAX_LEVEL_ROWS);
            int max = (int) Math.min(Math.floor(var.max()), MAX_LEVEL_ROWS);

            for (int value = min; value <= max; value++) {
                if (value == 0 && containsZero(var)) {
                    continue;
                }

                NumberExpr bound = single.bind(var, value);
                NumberFormatter.Formatted formatted = format(bound, false, number);
                int color = colored ? levels.size() : NO_COLOR;

                levels.add(bound);
                rows.add(new Row(depth, rowName(var, value), rowLabel(var, value), formatted.value(), formatted.mode(), -1, color));
            }
        }
    }

    @NotNull
    private static List<NumberExpr.Var> steppedVars(NumberExpr expr) {
        return expr.vars().stream().filter((v) -> Double.isFinite(v.min()) && Double.isFinite(v.max())).toList();
    }

    private static boolean containsZero(NumberExpr.Var var) {
        return var.min() <= 0 && var.max() >= 0;
    }

    @NotNull
    private static NumberExpr unbound(NumberExpr expr) {
        NumberExpr result = expr;

        for (NumberExpr.Var var : steppedVars(expr)) {
            if (containsZero(var)) {
                result = result.bind(var, 0);
            }
        }

        return result;
    }

    @NotNull
    private static NumberText rowName(NumberExpr.Var var, int value) {
        if (!var.type().equals(NumberExpr.LEVEL)) {
            String key = NumberFormatter.varKey(var.type());
            List<NumberText> args = new ArrayList<>();

            if (value == 0) {
                return new NumberText.Key(key, var.args());
            }

            args.add(NumberText.num(Math.abs(value)));
            args.addAll(var.args());
            return new NumberText.Key(key + (value < 0 ? ".row.negative" : ".row"), args);
        }
        if (!var.args().isEmpty() && var.args().get(0) instanceof NumberText.Str s) {
            ResourceLocation id = ResourceLocation.tryParse(s.text());

            if (id != null) {
                return NumberText.key("enchantment." + id.getNamespace() + "." + id.getPath());
            }
        }

        return NumberText.key(CoreLang.Numbers.UNKNOWN.singular());
    }

    @NotNull
    private static NumberFormatter.Formatted format(NumberExpr expr, boolean showFormulas, TooltipNumber number) {
        return NumberFormatter.format(expr, showFormulas, number.percent(), number.limit());
    }

    @Nullable
    private static ChartData chart(List<NumberExpr> exprs, boolean hasBase, TooltipNumber number) {
        List<NumberDistribution> distributions = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        boolean hasMode = false;

        for (int s = 0; s < exprs.size(); s++) {
            NumberExpr limited = limited(exprs.get(s), number.limit());
            NumberDistribution distribution = limited.distribution();

            if (distribution == null) {
                return null;
            }
            if (distribution.single().isPresent()) {
                continue;
            }

            hasMode |= distribution.mode().isPresent();
            distributions.add(distribution);
            if (hasBase) {
                levels.add(s == 0 ? NO_COLOR : s - 1);
            } else {
                levels.add(s);
            }
        }

        if (!hasMode) {
            return null;
        }

        List<Column> columns = columns(distributions);

        if (columns.isEmpty()) {
            return null;
        }

        List<SeriesData> series = new ArrayList<>();
        double highest = 0;

        for (int s = 0; s < distributions.size(); s++) {
            List<Double> probabilities = new ArrayList<>();

            for (int i = 0; i < columns.size(); i++) {
                probabilities.add(probability(distributions.get(s), columns.get(i), i == columns.size() - 1));
            }

            int first = 0;
            int last = columns.size() - 1;

            while (first < last && probabilities.get(first) <= 0) {
                first++;
            }
            while (last > first && probabilities.get(last) <= 0) {
                last--;
            }
            if (first == last) {
                continue;
            }

            highest = Math.max(highest, probabilities.stream().mapToDouble(Double::doubleValue).max().orElse(0));
            series.add(new SeriesData(
                    levels.get(s),
                    probabilities,
                    modeColumns(distributions.get(s), probabilities, columns),
                    first,
                    last,
                    NumberFormatter.number(columns.get(first).lo, number.percent()),
                    NumberFormatter.number(columns.get(last).hi, number.percent())
            ));
        }

        if (series.isEmpty() || highest <= 0) {
            return null;
        }

        double scale = highest;

        series.replaceAll((d) -> new SeriesData(d.level, d.heights.stream().map((p) -> p / scale).toList(), d.modes, d.first, d.last, d.min, d.max));
        return new ChartData(series, columnWidth(columns.size()));
    }

    @NotNull
    private static List<Boolean> modeColumns(NumberDistribution distribution, List<Double> probabilities, List<Column> columns) {
        Optional<NumberMode> mode = distribution.mode();
        List<Boolean> modes = new ArrayList<>();

        if (mode.isPresent()) {
            for (int i = 0; i < columns.size(); i++) {
                modes.add(columns.get(i).intersects(mode.get().lo(), mode.get().hi(), i == columns.size() - 1));
            }

            return modes;
        }

        double max = probabilities.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        boolean flat = probabilities.stream().allMatch((p) -> p <= 0 || max - p <= TIE * max);

        for (double p : probabilities) {
            modes.add(!flat && max - p <= TIE * max);
        }

        return modes;
    }

    @NotNull
    private static NumberExpr limited(NumberExpr expr, @Nullable NumberInterval limit) {
        if (limit == null) {
            return expr;
        }

        return NumberExpr.clamp(expr, NumberExpr.constant(limit.lo()), NumberExpr.constant(limit.hi()));
    }

    @NotNull
    private static List<Column> columns(List<NumberDistribution> distributions) {
        TreeSet<Double> values = new TreeSet<>();
        double lo = Double.POSITIVE_INFINITY;
        double hi = Double.NEGATIVE_INFINITY;
        boolean discrete = true;

        for (NumberDistribution distribution : distributions) {
            values.addAll(distribution.atoms().keySet());

            NumberDistribution.Density density = distribution.density();

            if (density != null) {
                discrete = false;
                lo = Math.min(lo, quantile(density, TAIL * density.mass()));
                hi = Math.max(hi, quantile(density, (1 - TAIL) * density.mass()));
            }
        }

        if (!values.isEmpty()) {
            lo = Math.min(lo, values.first());
            hi = Math.max(hi, values.last());
        }
        if (!Double.isFinite(lo) || !Double.isFinite(hi)) {
            return List.of();
        }

        List<Column> columns = new ArrayList<>();
        boolean integers = values.stream().allMatch((v) -> v == Math.rint(v));

        if (discrete && integers && hi - lo < MAX_COLUMNS) {
            for (double v = lo; v <= hi; v++) {
                columns.add(new Column(v, v, true));
            }
        } else if (discrete && values.size() <= MAX_COLUMNS) {
            values.forEach((v) -> columns.add(new Column(v, v, true)));
        } else if (discrete && integers) {
            double size = Math.ceil((hi - lo + 1) / MAX_COLUMNS);

            for (double v = lo; v <= hi; v += size) {
                columns.add(new Column(v, Math.min(v + size - 1, hi), true));
            }
        } else {
            double width = (hi - lo) / MAX_COLUMNS;

            if (width <= 0) {
                columns.add(new Column(lo, hi, true));
            } else {
                for (int i = 0; i < MAX_COLUMNS; i++) {
                    columns.add(new Column(lo + i * width, lo + (i + 1) * width, false));
                }
            }
        }

        return columns;
    }

    private static double probability(NumberDistribution distribution, Column column, boolean last) {
        double p = 0;

        for (Map.Entry<Double, Double> atom : distribution.atoms().entrySet()) {
            double v = atom.getKey();

            if (column.intersects(v, v, last)) {
                p += atom.getValue();
            }
        }

        NumberDistribution.Density density = distribution.density();

        if (density != null && !column.closed) {
            p += density.cdf().applyAsDouble(column.hi) - density.cdf().applyAsDouble(column.lo);
        }

        return p / column.size();
    }

    private static double quantile(NumberDistribution.Density density, double target) {
        double lo = Double.isFinite(density.lo()) ? density.lo() : -1e7;
        double hi = Double.isFinite(density.hi()) ? density.hi() : 1e7;

        for (int i = 0; i < 100; i++) {
            double mid = (lo + hi) / 2;

            if (density.cdf().applyAsDouble(mid) < target) {
                lo = mid;
            } else {
                hi = mid;
            }
        }

        return (lo + hi) / 2;
    }

    private static int columnWidth(int columns) {
        if (columns <= 16) {
            return 4;
        } else if (columns <= 32) {
            return 3;
        }

        return 2;
    }
}
