package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

interface CallContext {
    void traceCase(String label, MethodInsnNode call);

    boolean isConcrete(Value value);

    boolean isTainted(Value value);

    Value wrap(@Nullable Object value);

    @Nullable
    Value invoke(Value.Closure closure, List<Value> args, int depth);

    @Nullable
    Value execute(Class<?> owner, MethodNode method, List<Value> args, int depth);

    @Nullable
    Value concreteCall(Call call, boolean ownedResult);

    @Nullable
    Value combine(List<Value> results);

    Object[] javaArgs(List<Value> args, Class<?>[] types, int offset);

    Interpreter.Mode mode();

    int pick();

    NumberExpr expr(Value value);

    NumberExpr add(NumberExpr a, NumberExpr b);

    NumberExpr sub(NumberExpr a, NumberExpr b);

    Value draw(NumberExpr expr, Value.Kind kind);

    void own(Object object);

    void stackPart(Object part, Value.Stack stack);

    Value.Stack shadow(ItemStack real);

    List<Value> shadowed(List<Value> args);
}
