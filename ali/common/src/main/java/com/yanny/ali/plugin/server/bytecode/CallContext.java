package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.List;

interface CallContext {
    boolean isConcrete(Value value);

    boolean isTainted(Value value);

    Value wrap(@Nullable Object value);

    Value wrap(@Nullable Object value, Class<?> type);

    @Nullable
    Object toJava(Value value, Class<?> type);

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

    boolean isOwned(@Nullable Object object);

    boolean isFrozen(Object object);

    void taint(@Nullable Object object);

    CollState coll(Object object);

    int removals(Object object);

    @Nullable
    Value.Draws shuffled(Object object);

    @Nullable
    int[] randomIndexSet(Object object);

    boolean isPool(@Nullable Object draw);

    @Nullable
    NumberExpr drawCount(@Nullable Object draw);

    @Nullable
    Value.Offers offers(Object collection);

    @Nullable
    Subject subject();

    @Nullable
    Value selfField(String field);

    void putSelfField(String field, Value value);

    @Nullable
    Integer levelKey(Object listings);

    @Nullable
    Object listingSource(Object listing);

    @Nullable
    Integer poolKey(List<Object> listings);

    BoundOffer boundOffer(Object element, MethodInsnNode call, List<Object> args);

    @Nullable
    RegistryAccess registries();

    Collection<TagKey<?>> tags();

    List<Decision> decisions();

    boolean decide(Decision.Kind kind, @Nullable Object subject, @Nullable Object value, String reason);

    @Nullable
    Value jdk(Call call, Class<?> declaring);
}
