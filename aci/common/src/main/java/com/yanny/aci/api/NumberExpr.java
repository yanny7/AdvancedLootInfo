package com.yanny.aci.api;

import com.yanny.aci.number.NumberEvaluator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.UnaryOperator;

public sealed interface NumberExpr {
    Identifier LEVEL = Identifier.fromNamespaceAndPath("aci", "level");
    Identifier SCORE = Identifier.fromNamespaceAndPath("aci", "score");

    record Const(double value) implements NumberExpr {
    }

    record Range(@Nullable NumberExpr min, @Nullable NumberExpr max, boolean minClosed, boolean maxClosed) implements NumberExpr {
    }

    record Var(Identifier type, List<NumberText> args, double min, double max) implements NumberExpr {
        public Var {
            args = List.copyOf(args);
        }
    }

    record Lookup(NumberExpr index, List<NumberExpr> values, @Nullable NumberExpr fallback) implements NumberExpr {
        public Lookup {
            values = List.copyOf(values);

            if (values.isEmpty()) {
                throw new IllegalArgumentException("Lookup needs at least one value");
            }
        }
    }

    record Fn(Identifier id, List<NumberExpr> args) implements NumberExpr {
        public Fn {
            args = List.copyOf(args);
        }

        @Nullable
        public NumberFunction function() {
            return NumberFunctions.get(id);
        }
    }

    record Branch(int condition, NumberExpr value) {
    }

    record Cond(List<Branch> branches, NumberExpr otherwise) implements NumberExpr {
        public Cond {
            branches = List.copyOf(branches);
        }
    }

    record WeightedEntry(double weight, NumberExpr value) {
    }

    record Weighted(List<WeightedEntry> entries) implements NumberExpr {
        public Weighted {
            entries = List.copyOf(entries);

            if (entries.isEmpty()) {
                throw new IllegalArgumentException("Weighted needs at least one entry");
            }
        }

        public double totalWeight() {
            return entries.stream().mapToDouble(WeightedEntry::weight).sum();
        }
    }

    record Opaque(String typeId) implements NumberExpr {
    }

    @NotNull
    static NumberExpr constant(double value) {
        return new Const(value);
    }

    @NotNull
    static NumberExpr range(double min, double max) {
        return new Range(constant(min), constant(max), true, true);
    }

    @NotNull
    static NumberExpr range(@Nullable NumberExpr min, @Nullable NumberExpr max, boolean minClosed, boolean maxClosed) {
        return new Range(min, max, minClosed, maxClosed);
    }

    @NotNull
    static NumberExpr atLeast(double min) {
        return new Range(constant(min), null, true, false);
    }

    @NotNull
    static NumberExpr atMost(double max) {
        return new Range(null, constant(max), false, true);
    }

    @NotNull
    static NumberExpr fn(Identifier id, NumberExpr... args) {
        return fn(id, List.of(args));
    }

    @NotNull
    static NumberExpr fn(Identifier id, List<NumberExpr> args) {
        NumberFunction function = NumberFunctions.get(id);

        if (function == null) {
            return new Fn(id, args);
        }
        if (args.size() < function.minArity() || args.size() > function.maxArity()) {
            throw new IllegalArgumentException(id + " got " + args.size() + " arguments");
        }
        if (args.stream().allMatch(Const.class::isInstance)) {
            OptionalDouble value = function.evaluate(args.stream().mapToDouble((a) -> ((Const) a).value).toArray());

            if (value.isPresent()) {
                return constant(value.getAsDouble());
            }
        }

        NumberExpr simplified = function.simplify(args);

        if (simplified != null) {
            return simplified;
        }

        NumberExpr expanded = function.expand(args);

        if (expanded instanceof Const) {
            return expanded;
        }

        return new Fn(id, args);
    }

    @NotNull
    static NumberExpr add(NumberExpr... args) {
        return fn(NumberFunctions.ADD, args);
    }

    @NotNull
    static NumberExpr sub(NumberExpr a, NumberExpr b) {
        return fn(NumberFunctions.SUB, a, b);
    }

    @NotNull
    static NumberExpr mul(NumberExpr... args) {
        return fn(NumberFunctions.MUL, args);
    }

    @NotNull
    static NumberExpr div(NumberExpr a, NumberExpr b) {
        return fn(NumberFunctions.DIV, a, b);
    }

    @NotNull
    static NumberExpr min(NumberExpr... args) {
        return fn(NumberFunctions.MIN, args);
    }

    @NotNull
    static NumberExpr max(NumberExpr... args) {
        return fn(NumberFunctions.MAX, args);
    }

    @NotNull
    static NumberExpr clamp(NumberExpr value, NumberExpr min, NumberExpr max) {
        return fn(NumberFunctions.CLAMP, value, min, max);
    }

    @NotNull
    static NumberExpr uniformInt(NumberExpr min, NumberExpr max) {
        return fn(NumberFunctions.UNIFORM_INT, min, max);
    }

    @NotNull
    static NumberExpr uniformInt(double min, double max) {
        return uniformInt(constant(min), constant(max));
    }

    @NotNull
    static NumberExpr uniformFloat(double min, double max) {
        return fn(NumberFunctions.UNIFORM_FLOAT, constant(min), constant(max));
    }

    @NotNull
    static NumberExpr binomial(NumberExpr n, NumberExpr p) {
        return fn(NumberFunctions.BINOMIAL, n, p);
    }

    @NotNull
    static Var level(String enchantmentId, int maxLevel) {
        return new Var(LEVEL, List.of(NumberText.str(enchantmentId)), 0, maxLevel);
    }

    @NotNull
    static Var score(NumberText target, String objective) {
        return new Var(SCORE, List.of(target, NumberText.str(objective)), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @NotNull
    static NumberExpr lookup(NumberExpr index, List<NumberExpr> values, @Nullable NumberExpr fallback) {
        if (index instanceof Const c) {
            int i = (int) c.value;

            if (i >= 0 && i < values.size()) {
                return values.get(i);
            }

            return fallback != null ? fallback : values.get(values.size() - 1);
        }

        return new Lookup(index, values, fallback);
    }

    @NotNull
    static NumberExpr cond(List<Branch> branches, NumberExpr otherwise) {
        if (branches.isEmpty()) {
            return otherwise;
        }
        if (otherwise instanceof Cond inner) {
            List<Branch> merged = new ArrayList<>(branches);

            merged.addAll(inner.branches());
            return new Cond(merged, inner.otherwise());
        }

        return new Cond(branches, otherwise);
    }

    @NotNull
    static NumberExpr weighted(List<WeightedEntry> entries) {
        return entries.size() == 1 ? entries.get(0).value : new Weighted(entries);
    }

    @NotNull
    static NumberExpr opaque(String typeId) {
        return new Opaque(typeId);
    }

    @NotNull
    default NumberInterval bounds() {
        return NumberEvaluator.bounds(this);
    }

    @Nullable
    default NumberDistribution distribution() {
        return NumberEvaluator.distribution(this);
    }

    @NotNull
    default Optional<NumberMode> mode() {
        NumberDistribution distribution = distribution();

        return distribution != null ? distribution.mode() : Optional.empty();
    }

    default boolean isSimple() {
        if (this instanceof Const) {
            return true;
        }
        if (this instanceof Range r) {
            boolean minSimple = r.min == null || (r.min instanceof Const && r.minClosed);
            boolean maxSimple = r.max == null || (r.max instanceof Const && r.maxClosed);

            return minSimple && maxSimple;
        }
        if (this instanceof Fn f) {
            NumberFunction function = f.function();

            return function != null && function.isSimple(f.args);
        }

        return false;
    }

    @NotNull
    default Set<Var> vars() {
        Set<Var> vars = new LinkedHashSet<>();

        transform((e) -> {
            if (e instanceof Var v) {
                vars.add(v);
            }
            return e;
        });
        return vars;
    }

    @NotNull
    default NumberExpr bind(Var variable, double value) {
        return transform((e) -> e.equals(variable) ? constant(value) : e);
    }

    @NotNull
    default NumberExpr shiftConditions(int offset) {
        if (offset == 0) {
            return this;
        }

        return transform((e) -> {
            if (e instanceof Cond c) {
                return cond(c.branches.stream().map((b) -> new Branch(b.condition < 0 ? b.condition : b.condition + offset, b.value)).toList(), c.otherwise);
            }

            return e;
        });
    }

    @NotNull
    default NumberExpr transform(UnaryOperator<NumberExpr> mapper) {
        NumberExpr rebuilt;

        if (this instanceof Range r) {
            rebuilt = range(r.min != null ? r.min.transform(mapper) : null, r.max != null ? r.max.transform(mapper) : null, r.minClosed, r.maxClosed);
        } else if (this instanceof Lookup l) {
            rebuilt = lookup(
                    l.index.transform(mapper),
                    l.values.stream().map((v) -> v.transform(mapper)).toList(),
                    l.fallback != null ? l.fallback.transform(mapper) : null
            );
        } else if (this instanceof Fn f) {
            rebuilt = fn(f.id, f.args.stream().map((a) -> a.transform(mapper)).toList());
        } else if (this instanceof Cond c) {
            rebuilt = cond(
                    c.branches.stream().map((b) -> new Branch(b.condition, b.value.transform(mapper))).toList(),
                    c.otherwise.transform(mapper)
            );
        } else if (this instanceof Weighted w) {
            rebuilt = weighted(w.entries.stream().map((e) -> new WeightedEntry(e.weight, e.value.transform(mapper))).toList());
        } else {
            rebuilt = this;
        }

        return mapper.apply(rebuilt);
    }

    default void encode(FriendlyByteBuf buf) {
        if (this instanceof Const c) {
            buf.writeByte(0);
            buf.writeDouble(c.value);
        } else if (this instanceof Range r) {
            buf.writeByte(1);
            buf.writeNullable(r.min, NumberExpr::write);
            buf.writeNullable(r.max, NumberExpr::write);
            buf.writeBoolean(r.minClosed);
            buf.writeBoolean(r.maxClosed);
        } else if (this instanceof Var v) {
            buf.writeByte(2);
            buf.writeIdentifier(v.type);
            NumberText.encodeList(buf, v.args);
            buf.writeDouble(v.min);
            buf.writeDouble(v.max);
        } else if (this instanceof Lookup l) {
            buf.writeByte(3);
            l.index.encode(buf);
            encodeList(buf, l.values);
            buf.writeNullable(l.fallback, NumberExpr::write);
        } else if (this instanceof Fn f) {
            buf.writeByte(4);
            buf.writeIdentifier(f.id);
            encodeList(buf, f.args);
        } else if (this instanceof Cond c) {
            buf.writeByte(5);
            buf.writeVarInt(c.branches.size());
            c.branches.forEach((b) -> {
                buf.writeVarInt(b.condition);
                b.value.encode(buf);
            });
            c.otherwise.encode(buf);
        } else if (this instanceof Weighted w) {
            buf.writeByte(6);
            buf.writeVarInt(w.entries.size());
            w.entries.forEach((e) -> {
                buf.writeDouble(e.weight);
                e.value.encode(buf);
            });
        } else if (this instanceof Opaque o) {
            buf.writeByte(7);
            buf.writeUtf(o.typeId);
        }
    }

    @NotNull
    static NumberExpr decode(FriendlyByteBuf buf) {
        byte tag = buf.readByte();

        return switch (tag) {
            case 0 -> new Const(buf.readDouble());
            case 1 -> new Range(buf.readNullable(NumberExpr::decode), buf.readNullable(NumberExpr::decode), buf.readBoolean(), buf.readBoolean());
            case 2 -> new Var(buf.readIdentifier(), NumberText.decodeList(buf), buf.readDouble(), buf.readDouble());
            case 3 -> new Lookup(decode(buf), decodeList(buf), buf.readNullable(NumberExpr::decode));
            case 4 -> new Fn(buf.readIdentifier(), decodeList(buf));
            case 5 -> {
                int size = buf.readVarInt();
                List<Branch> branches = new ArrayList<>(size);

                for (int i = 0; i < size; i++) {
                    branches.add(new Branch(buf.readVarInt(), decode(buf)));
                }

                yield new Cond(branches, decode(buf));
            }
            case 6 -> {
                int size = buf.readVarInt();
                List<WeightedEntry> entries = new ArrayList<>(size);

                for (int i = 0; i < size; i++) {
                    entries.add(new WeightedEntry(buf.readDouble(), decode(buf)));
                }

                yield new Weighted(entries);
            }
            case 7 -> new Opaque(buf.readUtf());
            default -> throw new IllegalStateException("Unknown number expression tag " + tag);
        };
    }

    private static void write(FriendlyByteBuf buf, NumberExpr expr) {
        expr.encode(buf);
    }

    private static void encodeList(FriendlyByteBuf buf, List<NumberExpr> list) {
        buf.writeVarInt(list.size());
        list.forEach((e) -> e.encode(buf));
    }

    @NotNull
    private static List<NumberExpr> decodeList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<NumberExpr> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(decode(buf));
        }

        return list;
    }
}
