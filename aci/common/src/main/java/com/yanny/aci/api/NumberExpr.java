package com.yanny.aci.api;

import com.yanny.aci.number.NumberEvaluator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

public sealed interface NumberExpr {
    ResourceLocation LEVEL = ResourceLocation.fromNamespaceAndPath("aci", "level");
    ResourceLocation SCORE = ResourceLocation.fromNamespaceAndPath("aci", "score");

    record Const(double value) implements NumberExpr {
    }

    record Range(@Nullable NumberExpr min, @Nullable NumberExpr max, boolean minClosed, boolean maxClosed) implements NumberExpr {
    }

    record Var(ResourceLocation type, List<NumberText> args, double min, double max) implements NumberExpr {
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

    record Fn(ResourceLocation id, List<NumberExpr> args) implements NumberExpr {
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
    static NumberExpr fn(ResourceLocation id, NumberExpr... args) {
        return fn(id, List.of(args));
    }

    @NotNull
    static NumberExpr fn(ResourceLocation id, List<NumberExpr> args) {
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
        if (index instanceof Const(double value)) {
            int i = (int) value;

            if (i >= 0 && i < values.size()) {
                return values.get(i);
            }

            return fallback != null ? fallback : values.getLast();
        }

        return new Lookup(index, values, fallback);
    }

    @NotNull
    static NumberExpr cond(List<Branch> branches, NumberExpr otherwise) {
        if (branches.isEmpty()) {
            return otherwise;
        }
        if (otherwise instanceof Cond(List<Branch> branches1, NumberExpr otherwise1)) {
            List<Branch> merged = new ArrayList<>(branches);

            merged.addAll(branches1);
            return new Cond(merged, otherwise1);
        }

        return new Cond(branches, otherwise);
    }

    @NotNull
    static NumberExpr weighted(List<WeightedEntry> entries) {
        return entries.size() == 1 ? entries.getFirst().value : new Weighted(entries);
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
        switch (this) {
            case Const ignored -> {
                return true;
            }
            case Range(NumberExpr min, NumberExpr max, boolean minClosed, boolean maxClosed) -> {
                boolean minSimple = min == null || (min instanceof Const && minClosed);
                boolean maxSimple = max == null || (max instanceof Const && maxClosed);

                return minSimple && maxSimple;
            }
            case Fn f -> {
                NumberFunction function = f.function();

                return function != null && function.isSimple(f.args);
            }
            default -> {}
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
            if (e instanceof Cond(List<Branch> branches, NumberExpr otherwise)) {
                return cond(branches.stream().map((b) -> new Branch(b.condition < 0 ? b.condition : b.condition + offset, b.value)).toList(), otherwise);
            }

            return e;
        });
    }

    @NotNull
    default NumberExpr transform(UnaryOperator<NumberExpr> mapper) {
        NumberExpr rebuilt = switch (this) {
            case Range(NumberExpr min, NumberExpr max, boolean minClosed, boolean maxClosed) ->
                    range(min != null ? min.transform(mapper) : null, max != null ? max.transform(mapper) : null, minClosed, maxClosed);
            case Lookup(NumberExpr index, List<NumberExpr> values, NumberExpr fallback) -> lookup(
                    index.transform(mapper),
                    values.stream().map((v) -> v.transform(mapper)).toList(),
                    fallback != null ? fallback.transform(mapper) : null
            );
            case Fn(ResourceLocation id, List<NumberExpr> args) ->
                    fn(id, args.stream().map((a) -> a.transform(mapper)).toList());
            case Cond(List<Branch> branches, NumberExpr otherwise) -> cond(
                    branches.stream().map((b) -> new Branch(b.condition, b.value.transform(mapper))).toList(),
                    otherwise.transform(mapper)
            );
            case Weighted(List<WeightedEntry> entries) ->
                    weighted(entries.stream().map((e) -> new WeightedEntry(e.weight, e.value.transform(mapper))).toList());
            default -> this;
        };

        return mapper.apply(rebuilt);
    }

    default void encode(FriendlyByteBuf buf) {
        switch (this) {
            case Const c -> {
                buf.writeByte(0);
                buf.writeDouble(c.value);
            }
            case Range r -> {
                buf.writeByte(1);
                buf.writeNullable(r.min, NumberExpr::write);
                buf.writeNullable(r.max, NumberExpr::write);
                buf.writeBoolean(r.minClosed);
                buf.writeBoolean(r.maxClosed);
            }
            case Var v -> {
                buf.writeByte(2);
                buf.writeResourceLocation(v.type);
                NumberText.encodeList(buf, v.args);
                buf.writeDouble(v.min);
                buf.writeDouble(v.max);
            }
            case Lookup l -> {
                buf.writeByte(3);
                l.index.encode(buf);
                encodeList(buf, l.values);
                buf.writeNullable(l.fallback, NumberExpr::write);
            }
            case Fn f -> {
                buf.writeByte(4);
                buf.writeResourceLocation(f.id);
                encodeList(buf, f.args);
            }
            case Cond c -> {
                buf.writeByte(5);
                buf.writeVarInt(c.branches.size());
                c.branches.forEach((b) -> {
                    buf.writeVarInt(b.condition);
                    b.value.encode(buf);
                });
                c.otherwise.encode(buf);
            }
            case Weighted w -> {
                buf.writeByte(6);
                buf.writeVarInt(w.entries.size());
                w.entries.forEach((e) -> {
                    buf.writeDouble(e.weight);
                    e.value.encode(buf);
                });
            }
            case Opaque o -> {
                buf.writeByte(7);
                buf.writeUtf(o.typeId);
            }
            default -> {
            }
        }
    }

    @NotNull
    static NumberExpr decode(FriendlyByteBuf buf) {
        byte tag = buf.readByte();

        return switch (tag) {
            case 0 -> new Const(buf.readDouble());
            case 1 -> new Range(buf.readNullable(NumberExpr::decode), buf.readNullable(NumberExpr::decode), buf.readBoolean(), buf.readBoolean());
            case 2 -> new Var(buf.readResourceLocation(), NumberText.decodeList(buf), buf.readDouble(), buf.readDouble());
            case 3 -> new Lookup(decode(buf), decodeList(buf), buf.readNullable(NumberExpr::decode));
            case 4 -> new Fn(buf.readResourceLocation(), decodeList(buf));
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
