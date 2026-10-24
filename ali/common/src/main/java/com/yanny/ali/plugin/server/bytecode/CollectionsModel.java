package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberInterval;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;
import java.util.stream.IntStream;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.pick;

final class CollectionsModel implements MethodModel {
    static final MethodModel REMOVE_CHOICE = CollectionsModel::removeChoice;

    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        boolean isStatic = insn.getOpcode() == Opcodes.INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && insn.name.equals("get")
                && insn.desc.equals("(I)Ljava/lang/Object;") && args.get(1) instanceof Value.Num num) {
            context.traceCase("jdk.listPick", insn);
            return pick(list, num);
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && context.isOwned(list)
                && insn.name.equals("remove") && insn.desc.equals("(I)Ljava/lang/Object;")
                && (args.get(1) instanceof Value.Num || context.removals(list) > 0 || context.traderRun() != null && list.size() == 1)) {
            context.traceCase("jdk.removeRandom", insn);
            return removeRandom(context, list, args.get(1));
        }
        if (isStatic && insn.owner.equals("java/util/Collections") && insn.name.equals("shuffle") && args.get(0) instanceof Value.Known known
                && known.value() instanceof List<?> list && context.isOwned(list) && list.size() <= Limits.OPTIONS) {
            if (!list.isEmpty()) {
                CollState coll = context.coll(list);

                coll.shuffled = new Value.Draws(list, new ArrayList<>(list), list.size());
                coll.pool = true;
            }

            context.traceCase("jdk.shuffle", insn);
            return null;
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && context.shuffled(known.value()) != null) {
            return shuffled(context, insn, args, Objects.requireNonNull(context.shuffled(known.value())));
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof Set<?> set && context.isOwned(set)
                && insn.name.equals("add") && insn.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Num num) {
            context.traceCase("jdk.addRandom", insn);
            return addRandom(context, set, num);
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && context.randomIndexSet(known.value()) != null) {
            int[] draws = Objects.requireNonNull(context.randomIndexSet(known.value()));

            context.traceCase("jdk.randomSet", insn);
            return switch (insn.name + insn.desc) {
                case "size()I" -> new Value.Known(draws[1]);
                case "iterator()Ljava/util/Iterator;" -> new Value.Draws(known.value(), IntStream.range(0, draws[0]).boxed().map(Object.class::cast).toList(), draws[1]);
                default -> throw new Fail("change under a random choice");
            };
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && context.removals(known.value()) > 0) {
            int remaining = ((List<?>) known.value()).size() - context.removals(known.value());

            context.traceCase("jdk.removed", insn);
            return switch (insn.name + insn.desc) {
                case "size()I" -> new Value.Known(remaining);
                case "isEmpty()Z" -> new Value.Known(remaining == 0 ? 1 : 0);
                default -> throw new Fail("change under a random choice");
            };
        }

        return NO_OPINION;
    }

    private static NumberExpr withoutCap(NumberExpr count, int size) {
        if (count instanceof NumberExpr.Fn fn && fn.id().equals(NumberFunctions.MIN)) {
            List<NumberExpr> args = fn.args().stream().filter((a) -> !(a instanceof NumberExpr.Const c && c.value() >= size)).map((a) -> withoutCap(a, size)).toList();

            return args.size() == 1 ? args.get(0) : NumberExpr.fn(NumberFunctions.MIN, args.toArray(new NumberExpr[0]));
        }

        return count;
    }

    private static Value removeChoice(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();

        if (insn.getOpcode() != Opcodes.INVOKESTATIC && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && context.isOwned(list)
                && insn.name.equals("remove") && insn.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Choice choice
                && choice.options().size() == list.size() - context.removals(list) && new HashSet<Object>(list).containsAll(choice.options())) {
            context.coll(list).removals++;
            context.traceCase("undistributed.removeChoice", insn);
            return new Value.Known(1);
        }

        return NO_OPINION;
    }

    private static Value removeRandom(CallContext context, List<?> list, Value index) {
        int remaining = list.size() - context.removals(list);
        NumberInterval bounds = context.expr(index).bounds();

        if (remaining <= 0 || bounds.lo() != 0 || bounds.hi() != remaining - 1 || list.size() > Limits.OPTIONS) {
            throw new Fail("removal of a random element");
        }

        CollState coll = context.coll(list);

        coll.removals++;
        coll.pool = true;
        return new Value.Choice(new ArrayList<>(list), list);
    }

    private static Value shuffled(CallContext context, MethodInsnNode insn, List<Value> args, Value.Draws draws) {
        switch (insn.name + insn.desc) {
            case "size()I" -> {
                context.traceCase("jdk.shuffled", insn);
                return new Value.Known(draws.left);
            }
            case "isEmpty()Z" -> {
                context.traceCase("jdk.shuffled", insn);
                return new Value.Known(draws.left == 0 ? 1 : 0);
            }
            case "iterator()Ljava/util/Iterator;" -> {
                context.traceCase("jdk.shuffled", insn);
                return new Value.Draws(draws.pool, draws.options, draws.left);
            }
            case "get(I)Ljava/lang/Object;" -> {
                if (args.get(1) instanceof Value.Known index && (Integer) index.value() < draws.left) {
                    context.traceCase("jdk.shuffled", insn);
                    return new Value.Choice(draws.options, draws.pool);
                }
            }
            case "subList(II)Ljava/util/List;" -> {
                if (args.get(1) instanceof Value.Known from && Integer.valueOf(0).equals(from.value())
                        && (args.get(2) instanceof Value.Known || args.get(2) instanceof Value.Num)) {
                    NumberExpr to = context.expr(args.get(2));
                    Object view = new ArrayList<>();

                    if (args.get(2) instanceof Value.Num) {
                        context.coll(draws.pool).drawCount = withoutCap(to, draws.left);
                    }

                    context.own(view);
                    context.coll(view).shuffled = new Value.Draws(draws.pool, draws.options, Math.min(draws.left, (int) Math.floor(to.bounds().hi())));
                    context.traceCase("jdk.shuffled", insn);
                    return new Value.Known(view);
                }
            }
            default -> {
            }
        }

        context.traceCase("jdk.shuffled.fail", insn);
        throw new Fail("change under a random choice");
    }

    private static Value addRandom(CallContext context, Set<?> set, Value.Num index) {
        NumberInterval bounds = index.expr().bounds();
        int bound = (int) bounds.hi() + 1;
        CollState coll = context.coll(set);

        if (coll.randomIndexSet == null) {
            coll.randomIndexSet = new int[]{bound, 0};
        }

        int[] draws = coll.randomIndexSet;

        if (!set.isEmpty() || bounds.lo() != 0 || bounds.hi() != Math.rint(bounds.hi()) || draws[0] != bound || bound > Limits.OPTIONS) {
            throw new Fail("random set element");
        }

        draws[1] = Math.min(draws[1] + 1, bound);
        coll.pool = true;
        return new Value.Known(1);
    }
}
