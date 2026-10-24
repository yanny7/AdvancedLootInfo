package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.util.*;
import java.util.stream.IntStream;

final class Decisions implements Opcodes {
    private final List<Decision> taken = new ArrayList<>();
    @Nullable
    private final List<Boolean> script;
    private final OpaqueCalls opaque;
    private final NumberDomain numbers;
    private final ValueStore store;
    private final Heap heap;

    Decisions(@Nullable List<Boolean> script, OpaqueCalls opaque, NumberDomain numbers, ValueStore store, Heap heap) {
        this.script = script;
        this.opaque = opaque;
        this.numbers = numbers;
        this.store = store;
        this.heap = heap;
    }

    List<Decision> taken() {
        return taken;
    }

    boolean decide(Decision.Kind kind, @Nullable Object subject, @Nullable Object value, String reason) {
        if (script == null) {
            throw new Fail(reason);
        }

        Decision decision = new Decision(kind, subject, value, false);

        for (Decision earlier : taken) {
            if (earlier.sameTest(decision)) {
                return earlier.holds();
            }
            if (kind == Decision.Kind.EQUALS && earlier.kind() == kind && earlier.holds() && subject != null && subject.equals(earlier.subject())) {
                return false;
            }
        }

        boolean holds = taken.size() < script.size() && script.get(taken.size());

        if (counts(kind, holds) && taken.stream().filter((t) -> counts(t.kind(), t.holds())).count() >= Limits.DECISIONS) {
            opaque.markHeaviest();
            throw new Fail("too many branches");
        }

        taken.add(holds ? decision.negated() : decision);
        return holds;
    }

    private static boolean counts(Decision.Kind kind, boolean holds) {
        return switch (kind) {
            case EQUALS -> holds;
            case TEST, FOUND, RANDOM, UNKNOWN -> true;
        };
    }

    boolean decided(Value.Choice choice, java.util.function.Predicate<Object> test) {
        if (choice.options().stream().allMatch(test)) {
            return true;
        }
        if (choice.options().stream().noneMatch(test)) {
            return false;
        }

        boolean holds = decide(Decision.Kind.RANDOM, taken.size(), share(choice, test), "reference comparison of a random choice");

        narrow(choice, (o) -> test.test(o) == holds);
        return holds;
    }

    @Nullable
    private static Double share(Value.Choice choice, java.util.function.Predicate<Object> test) {
        return choice.draw() != null ? (double) choice.options().stream().filter(test).count() / choice.options().size() : null;
    }

    @Nullable
    private Value.Choice narrow(Value.Choice choice, java.util.function.Predicate<Object> keep) {
        List<Integer> kept = IntStream.range(0, choice.options().size()).filter((i) -> keep.test(choice.options().get(i))).boxed().toList();
        Map<Value, Value> narrowed = new IdentityHashMap<>();
        java.util.function.Predicate<Value> match = (v) -> v instanceof Value.Choice other && (other == choice || choice.draw() != null
                && other.draw() == choice.draw() && other.options().size() == choice.options().size());
        java.util.function.UnaryOperator<Value> with = (v) -> narrowed.computeIfAbsent(v, (o) -> narrowed((Value.Choice) o, kept));

        store.replace(match, with);

        return kept.size() > 1 ? (Value.Choice) narrowed(choice, kept) : null;
    }

    private Value narrowed(Value.Choice choice, List<Integer> kept) {
        List<Object> options = kept.stream().map(choice.options()::get).toList();

        return options.size() == 1 ? heap.wrap(options.get(0)) : new Value.Choice(options, choice.draw());
    }

    boolean isNull(Value value) {
        if (value instanceof Value.Known known) {
            return known.value() == null;
        }
        if (value instanceof Value.Choice choice) {
            return decided(choice, Objects::isNull);
        }
        if (value instanceof Value.Unknown unknown && unknown.found() != null && unknown.origin() != null) {
            return !decide(Decision.Kind.FOUND, unknown.origin(), unknown.found(), "null check of an unknown value");
        }
        if (value instanceof Value.Unknown unknown) {
            return !decide(Decision.Kind.UNKNOWN, unknown.origin(), Interpreter.NULL, "null check of an unknown value");
        }
        if (value instanceof Value.Uninit) {
            throw new Fail("null check of an unknown value");
        }

        return false;
    }

    boolean sameReference(Value a, Value b) {
        if (a instanceof Value.Known ka && b instanceof Value.Known kb) {
            return ka.value() == kb.value();
        }
        if (a instanceof Value.Choice choice && b instanceof Value.Known known) {
            return decided(choice, (o) -> o == known.value());
        }
        if (b instanceof Value.Choice choice && a instanceof Value.Known known) {
            return decided(choice, (o) -> o == known.value());
        }
        if (a instanceof Value.Unknown unknown && b instanceof Value.Known known) {
            return equality(unknown, known.value());
        }
        if (b instanceof Value.Unknown unknown && a instanceof Value.Known known) {
            return equality(unknown, known.value());
        }
        if (a instanceof Value.Unknown || b instanceof Value.Unknown) {
            return decide(Decision.Kind.UNKNOWN, null, null, "reference comparison of an unknown value");
        }
        if (a instanceof Value.Known ka && ka.value() == null || b instanceof Value.Known kb && kb.value() == null) {
            return false;
        }
        if (a instanceof Value.Known || b instanceof Value.Known) {
            throw new Fail("reference comparison of a symbolic value");
        }

        return a == b;
    }

    private boolean equality(Value.Unknown unknown, @Nullable Object value) {
        if (value == null) {
            return isNull(unknown);
        }
        if (unknown.origin() == null) {
            return decide(Decision.Kind.UNKNOWN, null, null, "reference comparison of an unknown value");
        }

        return decide(Decision.Kind.EQUALS, unknown.origin(), value, "reference comparison of an unknown value");
    }

    int switchCase(Value value, List<Integer> keys) {
        if (value instanceof Value.Known known) {
            return keys.indexOf((Integer) known.value());
        }
        if (value instanceof Value.Choice choice && choice.options().stream().allMatch(Integer.class::isInstance)) {
            Value.Choice rest = choice;

            for (int i = 0; i < keys.size(); i++) {
                Integer key = keys.get(i);
                Value.Choice current = rest;

                if (current.options().stream().allMatch(key::equals)) {
                    return i;
                }
                if (current.options().stream().anyMatch(key::equals)) {
                    if (decide(Decision.Kind.RANDOM, taken.size(), share(current, key::equals), "switch on a random choice")) {
                        narrow(current, key::equals);
                        return i;
                    }

                    rest = narrow(current, (o) -> !key.equals(o));

                    if (rest == null) {
                        return keys.indexOf(current.options().stream().filter((o) -> !key.equals(o)).findFirst().map(Integer.class::cast).orElseThrow());
                    }
                }
            }

            return -1;
        }
        if (value instanceof Value.Num num && num.expr().bounds().isPoint()) {
            return keys.indexOf((int) num.expr().bounds().lo());
        }
        NumberDomain.Drawn source = value instanceof Value.Num num ? numbers.drawn(num.expr()) : null;

        if (source != null && source.draw().values != null) {
            for (int i = 0; i < keys.size(); i++) {
                double key = keys.get(i) - source.offset();
                double probability = source.draw().probability(IFEQ, key);

                if (probability >= 1 || probability > 0 && decide(Decision.Kind.RANDOM, taken.size(), probability, "switch on a random value")) {
                    source.draw().narrow(IFEQ, key);
                    numbers.narrowed(source.draw());
                    return i;
                }

                source.draw().narrow(IFNE, key);
            }

            numbers.narrowed(source.draw());
            return -1;
        }
        if (value instanceof Value.Num num) {
            NumberInterval bounds = num.expr().bounds();
            List<Integer> possible = IntStream.range(0, keys.size()).filter((i) -> keys.get(i) >= bounds.lo() && keys.get(i) <= bounds.hi()).boxed().toList();
            boolean covered = possible.size() == Math.floor(bounds.hi()) - Math.ceil(bounds.lo()) + 1;
            int subject = taken.size();

            for (int i = 0; i < possible.size(); i++) {
                boolean last = covered && i == possible.size() - 1;

                if (last || decide(Decision.Kind.RANDOM, subject, keys.get(possible.get(i)), "switch on a random value")) {
                    return possible.get(i);
                }
            }

            return -1;
        }
        if (value instanceof Value.Unknown unknown) {
            for (int i = 0; i < keys.size(); i++) {
                boolean holds;

                if (unknown.origin() != null) {
                    holds = decide(Decision.Kind.EQUALS, unknown.origin(), keys.get(i), "switch on a value from the trader or world");
                } else {
                    holds = decide(Decision.Kind.UNKNOWN, null, null, "switch on a value from the trader or world");
                }

                if (holds) {
                    return i;
                }
            }

            return -1;
        }

        throw new Fail("symbolic switch");
    }

    boolean branch(int op, Value a, Value b) {
        if ((op == IFEQ || op == IFNE) && a instanceof Value.Unknown unknown && unknown.origin() != null && unknown.origin().isBoolean()
                && b instanceof Value.Known known && Integer.valueOf(0).equals(known.value())) {
            return test(unknown.origin()) == (op == IFNE);
        }
        if (a instanceof Value.Unknown || b instanceof Value.Unknown) {
            return decide(Decision.Kind.UNKNOWN, null, null, "value from the trader or world");
        }
        if (a instanceof Value.Known ka && b instanceof Value.Known kb) {
            int x = (Integer) ka.value();
            int y = (Integer) kb.value();

            return switch (op) {
                case IFEQ -> x == y;
                case IFNE -> x != y;
                case IFLT -> x < y;
                case IFGE -> x >= y;
                case IFGT -> x > y;
                default -> x <= y;
            };
        }

        Boolean onDraw = drawnBranch(op, a, b);

        if (onDraw != null) {
            return onDraw;
        }

        NumberInterval difference;

        if (a instanceof Value.Cmp cmp) {
            difference = cmp.left().bounds().minus(cmp.right().bounds());
        } else {
            difference = NumberDomain.expr(a).bounds().minus(NumberDomain.expr(b).bounds());
        }

        Boolean decided = Arithmetic.decide(op, difference);

        if (decided == null) {
            return decide(Decision.Kind.RANDOM, taken.size(), numbers.probability(op, a, b), "branch on a random value");
        }

        return decided;
    }

    @Nullable
    private Boolean drawnBranch(int op, Value a, Value b) {
        NumberExpr left = a instanceof Value.Cmp cmp ? cmp.left() : NumberDomain.expr(a);
        NumberExpr right = a instanceof Value.Cmp cmp ? cmp.right() : NumberDomain.expr(b);

        if (numbers.drawn(left) == null && numbers.drawn(right) != null) {
            return drawnBranch(NumberDomain.mirror(op), right, left);
        }

        return drawnBranch(op, left, right);
    }

    @Nullable
    private Boolean drawnBranch(int op, NumberExpr left, NumberExpr right) {
        NumberDomain.Drawn source = numbers.drawn(left);

        if (source == null || !(right instanceof NumberExpr.Const constant)) {
            return null;
        }

        NumberDomain.Draw draw = source.draw();
        double bound = constant.value() - source.offset();
        double probability = draw.probability(op, bound);

        if (probability <= 0) {
            return false;
        }
        if (probability >= 1) {
            return true;
        }

        boolean holds = decide(Decision.Kind.RANDOM, taken.size(), probability, "branch on a random value");

        draw.narrow(holds ? op : negation(op), bound);
        numbers.narrowed(draw);
        return holds;
    }

    private static int negation(int op) {
        return switch (op) {
            case IFEQ -> IFNE;
            case IFNE -> IFEQ;
            case IFLT -> IFGE;
            case IFGE -> IFLT;
            case IFGT -> IFLE;
            default -> IFGT;
        };
    }

    private boolean test(Origin origin) {
        if (origin.receiver() instanceof Origin subject && origin.args().size() == 1 && origin.args().get(0) != null && !(origin.args().get(0) instanceof Origin)) {
            return decide(origin.isEquality() ? Decision.Kind.EQUALS : Decision.Kind.TEST, subject, origin.args().get(0), "value from the trader or world");
        }

        return decide(Decision.Kind.UNKNOWN, origin, null, "value from the trader or world");
    }
}
