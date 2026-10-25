package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.num;

final class NumberModel implements MethodModel {
    static final MethodModel STATICS = NumberModel::statics;

    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.getOpcode() != Opcodes.INVOKESTATIC && call.args().get(0) instanceof Value.Num num) {
            return unbox(insn, num);
        }

        return NO_OPINION;
    }

    private static Value statics(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();

        if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
            return NO_OPINION;
        }
        if ((insn.owner.equals("java/lang/Math") || insn.owner.equals("java/lang/StrictMath")) && args.stream().anyMatch((v) -> v instanceof Value.Num)) {
            return math(context, insn, args);
        }
        if (insn.name.equals("valueOf") && args.size() == 1 && args.get(0) instanceof Value.Num num) {
            return insn.owner.equals("java/lang/String") ? Value.UNKNOWN : num;
        }

        return NO_OPINION;
    }

    private static Value unbox(MethodInsnNode insn, Value.Num num) {
        return switch (insn.name) {
            case "intValue", "shortValue", "byteValue" -> new Value.Num(num.expr(), Value.Kind.INT);
            case "longValue" -> new Value.Num(num.expr(), Value.Kind.LONG);
            case "floatValue" -> new Value.Num(num.expr(), Value.Kind.FLOAT);
            case "doubleValue" -> new Value.Num(num.expr(), Value.Kind.DOUBLE);
            default -> throw new Fail("number method " + insn.name);
        };
    }

    private static Value math(CallContext context, MethodInsnNode insn, List<Value> args) {
        Value.Kind kind = kindOf(Type.getReturnType(insn.desc));

        return switch (insn.name) {
            case "min" -> num(NumberExpr.min(context.expr(args.get(0)), context.expr(args.get(1))), kind);
            case "max" -> num(NumberExpr.max(context.expr(args.get(0)), context.expr(args.get(1))), kind);
            case "abs" -> num(NumberExpr.fn(NumberFunctions.ABS, context.expr(args.get(0))), kind);
            case "floor" -> num(NumberExpr.fn(NumberFunctions.FLOOR, context.expr(args.get(0))), kind);
            case "ceil" -> num(NumberExpr.fn(NumberFunctions.CEIL, context.expr(args.get(0))), kind);
            case "round" -> num(NumberExpr.fn(NumberFunctions.ROUND, context.expr(args.get(0))), kind);
            case "sqrt" -> num(NumberExpr.fn(NumberFunctions.SQRT, context.expr(args.get(0))), kind);
            case "pow" -> num(NumberExpr.fn(NumberFunctions.POW, context.expr(args.get(0)), context.expr(args.get(1))), kind);
            default -> throw new Fail("Math." + insn.name);
        };
    }

    private static Value.Kind kindOf(Type type) {
        return switch (type.getSort()) {
            case Type.LONG -> Value.Kind.LONG;
            case Type.FLOAT -> Value.Kind.FLOAT;
            case Type.DOUBLE -> Value.Kind.DOUBLE;
            default -> Value.Kind.INT;
        };
    }
}
