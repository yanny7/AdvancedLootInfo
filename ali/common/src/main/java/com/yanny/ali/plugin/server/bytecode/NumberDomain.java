package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.num;

final class NumberDomain implements Opcodes {
    private final Map<NumberExpr, Drawn> drawn = new IdentityHashMap<>();
    private final ValueStore store;

    NumberDomain(ValueStore store) {
        this.store = store;
    }

    @Nullable
    Drawn drawn(NumberExpr expr) {
        return drawn.get(expr);
    }

    Value draw(NumberExpr expr, Value.Kind kind) {
        if (expr instanceof NumberExpr.Fn fn && fn.args().size() == 2 && fn.args().get(0) instanceof NumberExpr.Const lo && fn.args().get(1) instanceof NumberExpr.Const hi) {
            if (fn.id().equals(NumberFunctions.UNIFORM_INT) && hi.value() - lo.value() < Interpreter.MAX_OPTIONS) {
                drawn.put(expr, new Drawn(Draw.discrete((int) lo.value(), (int) hi.value()), 0));
            } else if (fn.id().equals(NumberFunctions.UNIFORM_FLOAT) && hi.value() > lo.value()) {
                drawn.put(expr, new Drawn(Draw.continuous(lo.value(), hi.value()), 0));
            }
        }

        return num(expr, kind);
    }

    static NumberExpr expr(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Number number) {
            return NumberExpr.constant(number.doubleValue());
        } else if (value instanceof Value.Num num) {
            return num.expr();
        } else if (value instanceof Value.Choice choice && choice.options().stream().allMatch(Number.class::isInstance)) {
            return NumberExpr.weighted(choice.options().stream().map((o) -> new NumberExpr.WeightedEntry(1, NumberExpr.constant(((Number) o).doubleValue()))).toList());
        }

        throw new Fail(value instanceof Value.Unknown ? "value from the trader or world" : "not a number");
    }

    @NotNull
    NumberExpr add(NumberExpr a, NumberExpr b) {
        NumberExpr shifted = null;

        if (b instanceof NumberExpr.Const c) {
            shifted = shift(a, c.value());
        } else if (a instanceof NumberExpr.Const c) {
            shifted = shift(b, c.value());
        }

        return shifted != null ? shifted : NumberExpr.add(a, b);
    }

    @NotNull
    NumberExpr sub(NumberExpr a, NumberExpr b) {
        NumberExpr shifted = b instanceof NumberExpr.Const c ? shift(a, -c.value()) : null;

        return shifted != null ? shifted : NumberExpr.sub(a, b);
    }

    @Nullable
    private NumberExpr shift(NumberExpr value, double by) {
        if (value instanceof NumberExpr.Fn fn && (fn.id().equals(NumberFunctions.UNIFORM_INT) || fn.id().equals(NumberFunctions.UNIFORM_FLOAT))
                && fn.args().get(0) instanceof NumberExpr.Const lo && fn.args().get(1) instanceof NumberExpr.Const hi) {
            NumberExpr shifted = NumberExpr.fn(fn.id(), NumberExpr.constant(lo.value() + by), NumberExpr.constant(hi.value() + by));
            Drawn source = drawn.get(value);

            if (source != null) {
                drawn.put(shifted, new Drawn(source.draw(), source.offset() + by));
            }

            return shifted;
        }

        return null;
    }

    @Nullable
    Double probability(int op, Value a, Value b) {
        NumberExpr left = a instanceof Value.Cmp cmp ? cmp.left() : expr(a);
        NumberExpr right = a instanceof Value.Cmp cmp ? cmp.right() : expr(b);
        Double probability = uniformProbability(op, left, right);

        return probability != null ? probability : uniformProbability(mirror(op), right, left);
    }

    @Nullable
    private static Double uniformProbability(int op, NumberExpr draw, NumberExpr bound) {
        if (!(bound instanceof NumberExpr.Const constant) || !(draw instanceof NumberExpr.Fn fn) || fn.args().size() != 2
                || !(fn.args().get(0) instanceof NumberExpr.Const lo) || !(fn.args().get(1) instanceof NumberExpr.Const hi)) {
            return null;
        }

        double value = constant.value();

        if (fn.id().equals(NumberFunctions.UNIFORM_FLOAT) && hi.value() > lo.value()) {
            double below = Math.max(0, Math.min(1, (value - lo.value()) / (hi.value() - lo.value())));

            return switch (op) {
                case IFLT, IFLE -> below;
                case IFGT, IFGE -> 1 - below;
                case IFEQ -> 0.0;
                default -> 1.0;
            };
        }
        if (fn.id().equals(NumberFunctions.UNIFORM_INT) && hi.value() >= lo.value()) {
            double total = hi.value() - lo.value() + 1;
            double below = Math.max(0, Math.min(hi.value(), Math.ceil(value) - 1) - lo.value() + 1);
            double atMost = Math.max(0, Math.min(hi.value(), Math.floor(value)) - lo.value() + 1);
            double count = switch (op) {
                case IFLT -> below;
                case IFLE -> atMost;
                case IFGT -> total - atMost;
                case IFGE -> total - below;
                case IFEQ -> atMost - below;
                default -> total - (atMost - below);
            };

            return count / total;
        }

        return null;
    }

    static int mirror(int op) {
        return switch (op) {
            case IFLT -> IFGT;
            case IFGT -> IFLT;
            case IFLE -> IFGE;
            case IFGE -> IFLE;
            default -> op;
        };
    }

    void narrowed(Draw draw) {
        Map<NumberExpr, NumberExpr> replacements = new IdentityHashMap<>();

        drawn.forEach((expr, source) -> {
            if (source.draw() == draw) {
                replacements.put(expr, draw.expr(source.offset()));
            }
        });
        replacements.forEach((from, to) -> {
            if (!(to instanceof NumberExpr.Const)) {
                drawn.put(to, drawn.get(from));
            }
        });

        store.replaceInFrames((v) -> v instanceof Value.Num || v instanceof Value.Stack, (v) -> {
            if (v instanceof Value.Stack stack) {
                stack.count = substitute(stack.count, replacements);
                return stack;
            }

            Value.Num num = (Value.Num) v;
            NumberExpr expr = substitute(num.expr(), replacements);

            return expr == num.expr() ? num : num(expr, num.kind());
        });
    }

    private static NumberExpr substitute(NumberExpr expr, Map<NumberExpr, NumberExpr> replacements) {
        if (replacements.containsKey(expr)) {
            return replacements.get(expr);
        }
        if (expr instanceof NumberExpr.Fn fn) {
            List<NumberExpr> args = fn.args().stream().map((a) -> substitute(a, replacements)).toList();

            return IntStream.range(0, args.size()).allMatch((i) -> args.get(i) == fn.args().get(i)) ? expr : NumberExpr.fn(fn.id(), args);
        }
        if (expr instanceof NumberExpr.Weighted weighted) {
            List<NumberExpr.WeightedEntry> entries = weighted.entries().stream()
                    .map((e) -> new NumberExpr.WeightedEntry(e.weight(), substitute(e.value(), replacements))).toList();

            return IntStream.range(0, entries.size()).allMatch((i) -> entries.get(i).value() == weighted.entries().get(i).value()) ? expr : NumberExpr.weighted(entries);
        }

        return expr;
    }

    record Drawn(Draw draw, double offset) {
    }

    static final class Draw {
        @Nullable
        final List<Integer> values;
        double lo;
        double hi;

        private Draw(@Nullable List<Integer> values, double lo, double hi) {
            this.values = values;
            this.lo = lo;
            this.hi = hi;
        }

        @NotNull
        static Draw discrete(int lo, int hi) {
            return new Draw(IntStream.rangeClosed(lo, hi).boxed().collect(Collectors.toCollection(ArrayList::new)), lo, hi);
        }

        @NotNull
        static Draw continuous(double lo, double hi) {
            return new Draw(null, lo, hi);
        }

        double probability(int op, double bound) {
            if (values != null) {
                return values.isEmpty() ? 0 : (double) values.stream().filter((v) -> compares(op, v, bound)).count() / values.size();
            }
            if (hi <= lo) {
                return 0;
            }

            double below = Math.max(0, Math.min(1, (bound - lo) / (hi - lo)));

            return switch (op) {
                case IFLT, IFLE -> below;
                case IFGT, IFGE -> 1 - below;
                case IFEQ -> 0;
                default -> 1;
            };
        }

        NumberExpr expr(double offset) {
            if (values == null) {
                return NumberExpr.uniformFloat(lo + offset, hi + offset);
            }
            if (values.size() == 1) {
                return NumberExpr.constant(values.get(0) + offset);
            }

            int min = Collections.min(values);
            int max = Collections.max(values);

            if (max - min + 1 == values.size()) {
                return NumberExpr.uniformInt(min + offset, max + offset);
            }

            return NumberExpr.weighted(values.stream().map((v) -> new NumberExpr.WeightedEntry(1, NumberExpr.constant(v + offset))).toList());
        }

        void narrow(int op, double bound) {
            if (values != null) {
                values.removeIf((v) -> !compares(op, v, bound));
            } else if (op == IFLT || op == IFLE) {
                hi = Math.min(hi, bound);
            } else if (op == IFGT || op == IFGE) {
                lo = Math.max(lo, bound);
            }
        }

        private static boolean compares(int op, double value, double bound) {
            return switch (op) {
                case IFEQ -> value == bound;
                case IFNE -> value != bound;
                case IFLT -> value < bound;
                case IFGE -> value >= bound;
                case IFGT -> value > bound;
                default -> value <= bound;
            };
        }
    }
}
