package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.Arithmetic.doubleOf;
import static com.yanny.ali.plugin.server.bytecode.Arithmetic.floatOf;
import static com.yanny.ali.plugin.server.bytecode.Arithmetic.intOf;

final class RandomModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.getOpcode() != Opcodes.INVOKESTATIC && call.args().get(0) instanceof Value.Rng rng) {
            return random(context, call, rng);
        }

        return NO_OPINION;
    }

    @Nullable
    private static Value random(CallContext context, Call call, Value.Rng rng) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        Semantic semantic = rng.legacy() ? legacySemantic(insn) : sourceSemantic(insn);

        if (semantic == null) {
            if (!rng.legacy() && Resolution.declaration(RandomSource.class, insn.name, insn.desc, false) instanceof Resolution.Declaration declaration) {
                return context.execute(declaration.owner(), declaration.method(), args, call.depth());
            }

            throw new Fail("random method " + insn.name);
        }

        if (context.mode() == Interpreter.Mode.STUB) {
            return stubRandom(semantic, rng, args, context.pick());
        }

        return switch (semantic) {
            case BOUND -> context.draw(NumberExpr.uniformInt(NumberExpr.constant(0), context.sub(context.expr(args.get(1)), NumberExpr.constant(1))), Value.Kind.INT);
            case BETWEEN -> context.draw(NumberExpr.uniformInt(context.expr(args.get(1)), context.expr(args.get(2))), Value.Kind.INT);
            case ORIGIN -> context.draw(NumberExpr.uniformInt(context.expr(args.get(1)), context.sub(context.expr(args.get(2)), NumberExpr.constant(1))), Value.Kind.INT);
            case FLOAT -> context.draw(NumberExpr.uniformFloat(0, 1), Value.Kind.FLOAT);
            case FLOAT_BOUND -> context.draw(NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, NumberExpr.constant(0), context.expr(args.get(1))), Value.Kind.FLOAT);
            case FLOAT_ORIGIN -> context.draw(NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, context.expr(args.get(1)), context.expr(args.get(2))), Value.Kind.FLOAT);
            case DOUBLE -> context.draw(NumberExpr.uniformFloat(0, 1), Value.Kind.DOUBLE);
            case DOUBLE_BOUND -> context.draw(NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, NumberExpr.constant(0), context.expr(args.get(1))), Value.Kind.DOUBLE);
            case DOUBLE_ORIGIN -> context.draw(NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, context.expr(args.get(1)), context.expr(args.get(2))), Value.Kind.DOUBLE);
            case BOOLEAN -> context.draw(NumberExpr.uniformInt(0, 1), Value.Kind.INT);
            case INT -> throw new Fail("unbounded random int");
            case LONG -> throw new Fail("random long");
            case GAUSSIAN -> throw new Fail("random gaussian");
            case FORK -> rng;
            case IGNORED -> null;
        };
    }

    @Nullable
    private static Value stubRandom(Semantic semantic, Value.Rng rng, List<Value> args, int pick) {
        float fraction = StubRandom.fraction(pick);

        return switch (semantic) {
            case BOUND -> new Value.Known(StubRandom.bound(pick, intOf(args.get(1))));
            case BETWEEN -> new Value.Known(intOf(args.get(1)) + StubRandom.bound(pick, intOf(args.get(2)) - intOf(args.get(1)) + 1));
            case ORIGIN -> new Value.Known(intOf(args.get(1)) + StubRandom.bound(pick, intOf(args.get(2)) - intOf(args.get(1))));
            case FLOAT -> new Value.Known(fraction);
            case FLOAT_BOUND -> new Value.Known(fraction * floatOf(args.get(1)));
            case FLOAT_ORIGIN -> new Value.Known(floatOf(args.get(1)) + fraction * (floatOf(args.get(2)) - floatOf(args.get(1))));
            case DOUBLE -> new Value.Known((double) fraction);
            case DOUBLE_BOUND -> new Value.Known(fraction * doubleOf(args.get(1)));
            case DOUBLE_ORIGIN -> new Value.Known(doubleOf(args.get(1)) + fraction * (doubleOf(args.get(2)) - doubleOf(args.get(1))));
            case BOOLEAN -> new Value.Known(pick != 0 ? 1 : 0);
            case INT -> new Value.Known(pick);
            case LONG -> new Value.Known((long) pick);
            case GAUSSIAN -> new Value.Known(0D);
            case FORK -> rng;
            case IGNORED -> null;
        };
    }

    @Nullable
    private static Semantic sourceSemantic(MethodInsnNode call) {
        if (RuntimeNames.is(RuntimeNames.NEXT_INT_BOUND, call)) {
            return Semantic.BOUND;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_INT_BETWEEN, call)) {
            return Semantic.BETWEEN;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_INT_ORIGIN, call)) {
            return Semantic.ORIGIN;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_FLOAT, call)) {
            return Semantic.FLOAT;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_DOUBLE, call)) {
            return Semantic.DOUBLE;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_BOOLEAN, call)) {
            return Semantic.BOOLEAN;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_INT, call)) {
            return Semantic.INT;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_LONG, call)) {
            return Semantic.LONG;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_GAUSSIAN, call)) {
            return Semantic.GAUSSIAN;
        } else if (RuntimeNames.is(RuntimeNames.FORK, call)) {
            return Semantic.FORK;
        } else if (RuntimeNames.is(RuntimeNames.SET_SEED, call) || RuntimeNames.is(RuntimeNames.CONSUME_COUNT, call)) {
            return Semantic.IGNORED;
        }

        return null;
    }

    @Nullable
    private static Semantic legacySemantic(MethodInsnNode call) {
        return switch (call.name + call.desc) {
            case "nextInt(I)I" -> Semantic.BOUND;
            case "nextInt(II)I" -> Semantic.ORIGIN;
            case "nextInt()I" -> Semantic.INT;
            case "nextFloat()F" -> Semantic.FLOAT;
            case "nextFloat(F)F" -> Semantic.FLOAT_BOUND;
            case "nextFloat(FF)F" -> Semantic.FLOAT_ORIGIN;
            case "nextDouble()D" -> Semantic.DOUBLE;
            case "nextDouble(D)D" -> Semantic.DOUBLE_BOUND;
            case "nextDouble(DD)D" -> Semantic.DOUBLE_ORIGIN;
            case "nextBoolean()Z" -> Semantic.BOOLEAN;
            case "nextLong()J" -> Semantic.LONG;
            case "nextGaussian()D" -> Semantic.GAUSSIAN;
            case "setSeed(J)V" -> Semantic.IGNORED;
            default -> null;
        };
    }

    private enum Semantic {
        BOUND,
        BETWEEN,
        ORIGIN,
        FLOAT,
        FLOAT_BOUND,
        FLOAT_ORIGIN,
        DOUBLE,
        DOUBLE_BOUND,
        DOUBLE_ORIGIN,
        BOOLEAN,
        INT,
        LONG,
        GAUSSIAN,
        FORK,
        IGNORED,
    }
}
