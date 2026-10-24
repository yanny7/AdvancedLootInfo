package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

interface CallContext {
    void traceCase(String label, MethodInsnNode call);

    boolean isConcrete(Value value);

    Value wrap(@Nullable Object value);

    @Nullable
    Value invoke(Value.Closure closure, List<Value> args, int depth);
}
