package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

class FakeCallContext implements CallContext {
    final List<Invocation> invocations = new ArrayList<>();
    @Nullable
    Value invokeResult;

    static Call call(int opcode, String owner, String name, String desc, Value... args) {
        return new Call(new MethodInsnNode(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE), List.of(args), FakeCallContext.class.getClassLoader(), 3);
    }

    static Value.Closure closure() {
        Handle impl = new Handle(Opcodes.H_INVOKESTATIC, "mod/Lambdas", "lambda$0", "()Ljava/lang/Object;", false);

        return new Value.Closure(Supplier.class, "get", "()Ljava/lang/Object;", impl, List.of(), FakeCallContext.class.getClassLoader());
    }

    @Override
    public void traceCase(String label, MethodInsnNode call) {
    }

    @Override
    public boolean isConcrete(Value value) {
        return value instanceof Value.Known;
    }

    @Override
    public Value wrap(@Nullable Object value) {
        return new Value.Known(value);
    }

    @Nullable
    @Override
    public Value invoke(Value.Closure closure, List<Value> args, int depth) {
        invocations.add(new Invocation(closure, args, depth));
        return invokeResult;
    }

    record Invocation(Value.Closure closure, List<Value> args, int depth) {
    }
}
