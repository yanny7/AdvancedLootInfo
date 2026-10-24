package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

final class OptionalModel implements MethodModel {
    static final Set<String> READ_ONLY = Set.of("isPresent", "orElse", "orElseGet", "orElseThrow");

    private static final String OPTIONAL = Type.getInternalName(Optional.class);

    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        boolean isStatic = insn.getOpcode() == Opcodes.INVOKESTATIC;

        if (!insn.owner.equals(OPTIONAL)) {
            return NO_OPINION;
        }
        if (isStatic && (insn.name.equals("of") || insn.name.equals("ofNullable")) && args.get(0) instanceof Value.Stack stack) {
            context.traceCase("undistributed.optionalOf", insn);
            return stack;
        }
        if (!isStatic && args.get(0) instanceof Value.Stack stack) {
            context.traceCase("undistributed.optionalStack", insn);
            return optionalCall(context, call, stack);
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof Optional<?> optional
                && args.stream().skip(1).anyMatch((arg) -> arg instanceof Value.Closure || !context.isConcrete(arg))) {
            context.traceCase("undistributed.optionalConcrete", insn);
            return optionalCall(context, call, optional.isPresent() ? context.wrap(optional.get()) : null);
        }

        return NO_OPINION;
    }

    @Nullable
    private static Value optionalCall(CallContext context, Call call, @Nullable Value present) {
        List<Value> args = call.args();

        switch (call.insn().name) {
            case "isPresent" -> {
                return new Value.Known(present != null ? 1 : 0);
            }
            case "isEmpty" -> {
                return new Value.Known(present == null ? 1 : 0);
            }
            case "get", "orElseThrow" -> {
                if (present == null) {
                    throw new Fail("value of an empty Optional");
                }

                return present;
            }
            case "orElse" -> {
                return present != null ? present : args.get(1);
            }
            case "orElseGet" -> {
                return present != null ? present : applyClosure(context, args.get(1), List.of(), call.depth());
            }
            case "ifPresent" -> {
                if (present != null) {
                    applyClosure(context, args.get(1), List.of(present), call.depth());
                }

                return null;
            }
            case "map" -> {
                if (present == null) {
                    return new Value.Known(Optional.empty());
                }

                Value mapped = applyClosure(context, args.get(1), List.of(present), call.depth());

                if (mapped instanceof Value.Stack stack) {
                    return stack;
                }
                if (mapped == null || mapped instanceof Value.Known) {
                    return new Value.Known(Optional.ofNullable(mapped instanceof Value.Known known ? known.value() : null));
                }

                throw new Fail("symbolic result of Optional.map");
            }
            default -> throw new Fail("symbolic Optional." + call.insn().name);
        }
    }

    @Nullable
    private static Value applyClosure(CallContext context, Value function, List<Value> args, int depth) {
        if (!(function instanceof Value.Closure closure)) {
            throw new Fail("symbolic Optional function");
        }

        return context.invoke(closure, args, depth);
    }
}
