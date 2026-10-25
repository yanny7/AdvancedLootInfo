package com.yanny.ali.plugin.server.bytecode;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.CONSTRUCTOR;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.ITEM_STACK;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.concrete;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.limit;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.num;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.stackOf;

final class ItemStackModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.name.equals(CONSTRUCTOR)) {
            if (insn.owner.equals(ITEM_STACK)) {
                return construct(context, insn.desc, call.args());
            }

            return NO_OPINION;
        }

        Value receiver = insn.getOpcode() == Opcodes.INVOKESTATIC ? null : call.args().get(0);

        if (receiver instanceof Value.Stack stack) {
            return stackCall(context, call, stack, call.args());
        }
        if (receiver instanceof Value.Known known && known.value() instanceof ItemStack real) {
            return knownStackCall(context, call, real);
        }

        return NO_OPINION;
    }

    @Nullable
    static Value argumentCall(CallContext context, Call call) {
        List<Value> shadowedArgs = context.shadowed(call.args());
        List<Value.Stack> stacks = shadowedArgs.stream().filter(Value.Stack.class::isInstance).map(Value.Stack.class::cast).toList();
        Map<ItemStack, ItemStack> fixed = new IdentityHashMap<>();

        if (stacks.size() > 1) {
            Value.Stack kept = stacks.stream().filter((s) -> !isFixed(s)).findFirst().orElse(stacks.get(0));

            shadowedArgs = shadowedArgs.stream().map((v) -> v instanceof Value.Stack s && s != kept && isFixed(s) ? fixedStack(context, s, fixed) : v).toList();
        }

        Value result = singleStackArgumentCall(context, call, shadowedArgs);

        if (fixed.entrySet().stream().anyMatch((e) -> !ItemStack.matches(e.getKey(), e.getValue()))) {
            throw new Fail("stack argument changed");
        }

        return result;
    }

    @NotNull
    private static Value construct(CallContext context, String desc, List<Value> args) {
        Type[] params = Type.getArgumentTypes(desc);
        NumberExpr count = params.length >= 2 && params[1].getSort() == Type.INT ? context.expr(args.get(1)) : NumberExpr.constant(1);
        List<Object> items;

        if (args.get(0) instanceof Value.Choice choice) {
            items = choice.options();
        } else {
            Object item = concrete(args.get(0));

            if (item == null) {
                throw new Fail("null item");
            }

            items = List.of(item);
        }
        Constructor<?> constructor = Resolution.constructor(ItemStack.class, desc);
        List<ItemStack> templates = new ArrayList<>();

        for (Object item : items) {
            List<Value> copy = new ArrayList<>(args);

            copy.set(0, new Value.Known(item));

            if (params.length >= 2 && params[1].getSort() == Type.INT) {
                copy.set(1, new Value.Known(count instanceof NumberExpr.Const c ? (int) c.value() : 1));
            }
            if (!copy.stream().allMatch(context::isConcrete)) {
                throw new Fail("symbolic stack argument");
            }

            try {
                constructor.setAccessible(true);
                templates.add((ItemStack) constructor.newInstance(context.javaArgs(copy, constructor.getParameterTypes(), 0)));
            } catch (Throwable e) {
                throw new Fail("stack constructor failed");
            }
        }

        Value.Stack stack = new Value.Stack(templates, count, false);

        if (templates.size() > 1) {
            stack.tag = context.tags().stream().filter((t) -> t.isFor(Registries.ITEM) || t.isFor(Registries.BLOCK))
                    .<TagKey<? extends ItemLike>>map((t) -> (TagKey<? extends ItemLike>) t).filter((t) -> isTag(t, templates)).findFirst().orElse(null);
        }

        return limit(stack);
    }

    static boolean isTag(TagKey<? extends ItemLike> tag, List<ItemStack> stacks) {
        Set<Item> items = stacks.stream().filter((s) -> !s.isEmpty()).map(ItemStack::getItem).collect(Collectors.toSet());
        Set<Item> contents = NodeUtils.resolveItems(Either.right(tag)).stream().map(ItemStack::getItem).collect(Collectors.toSet());

        return items.equals(contents) && stacks.stream().allMatch(TradeTypes::isPlain);
    }

    @Nullable
    private static Value stackCall(CallContext context, Call call, Value.Stack stack, List<Value> args) {
        MethodInsnNode insn = call.insn();

        if (RuntimeNames.is(RuntimeNames.STACK_SET_COUNT, insn)) {
            stack.count = context.expr(args.get(1));
            return null;
        } else if (RuntimeNames.is(RuntimeNames.STACK_GROW, insn)) {
            stack.count = context.add(stack.count, context.expr(args.get(1)));
            return null;
        } else if (RuntimeNames.is(RuntimeNames.STACK_SHRINK, insn)) {
            stack.count = context.sub(stack.count, context.expr(args.get(1)));
            return null;
        } else if (RuntimeNames.is(RuntimeNames.STACK_GET_COUNT, insn)) {
            return num(stack.count, Value.Kind.INT);
        } else if (RuntimeNames.is(RuntimeNames.STACK_GET_ITEM, insn)) {
            List<Object> items = stack.options.stream().<Object>map(ItemStack::getItem).distinct().toList();

            return items.size() == 1 ? new Value.Known(items.get(0)) : new Value.Choice(items);
        } else if (RuntimeNames.is(RuntimeNames.STACK_COPY, insn)) {
            return stack.copy();
        } else if (RuntimeNames.is(RuntimeNames.STACK_COPY_WITH_COUNT, insn)) {
            Value.Stack copy = stack.copy();

            copy.count = context.expr(args.get(1));
            return copy;
        } else if (RuntimeNames.is(RuntimeNames.STACK_IS_EMPTY, insn)) {
            NumberInterval bounds = stack.count.bounds();

            if (stack.options.stream().noneMatch(ItemStack::isEmpty) && !bounds.isUnknown() && bounds.lo() >= 1) {
                return new Value.Known(0);
            }
            if (stack.count instanceof NumberExpr.Const c && c.value() <= 0) {
                return new Value.Known(1);
            }

            throw new Fail("stack emptiness");
        }

        return onTemplates(context, call, stack, args, 0);
    }

    @Nullable
    private static Value knownStackCall(CallContext context, Call call, ItemStack real) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();

        if (RuntimeNames.is(RuntimeNames.STACK_GET_COUNT, insn)) {
            return new Value.Known(real.getCount());
        } else if (RuntimeNames.is(RuntimeNames.STACK_GET_ITEM, insn)) {
            return context.wrap(real.getItem());
        } else if (RuntimeNames.is(RuntimeNames.STACK_IS_EMPTY, insn)) {
            return new Value.Known(real.isEmpty() ? 1 : 0);
        } else if (RuntimeNames.is(RuntimeNames.STACK_COPY, insn)) {
            return stackOf(real);
        } else if (RuntimeNames.is(RuntimeNames.STACK_COPY_WITH_COUNT, insn)) {
            Value.Stack copy = stackOf(real);

            copy.count = context.expr(args.get(1));
            return copy;
        }

        return stackCall(context, call, context.shadow(real), context.shadowed(args));
    }

    private static boolean isFixed(Value.Stack stack) {
        return stack.options.size() == 1 && stack.count instanceof NumberExpr.Const && !stack.randomTag && stack.destination == null && stack.decoration == null;
    }

    private static Value fixedStack(CallContext context, Value.Stack stack, Map<ItemStack, ItemStack> fixed) {
        ItemStack real = stack.options.get(0).copy();

        real.setCount((int) ((NumberExpr.Const) stack.count).value());
        context.own(real);
        fixed.put(real, real.copy());
        return new Value.Known(real);
    }

    @Nullable
    private static Value singleStackArgumentCall(CallContext context, Call call, List<Value> shadowedArgs) {
        int index = -1;

        for (int i = 0; i < shadowedArgs.size(); i++) {
            if (shadowedArgs.get(i) instanceof Value.Stack) {
                if (index >= 0) {
                    throw new Fail("several stack arguments");
                }

                index = i;
            }
        }

        Value.Stack stack = (Value.Stack) shadowedArgs.get(index);

        if (shadowedArgs.stream().filter((v) -> v != stack).allMatch(context::isConcrete)) {
            return onTemplates(context, call, stack, shadowedArgs, index);
        }

        return unknownStackChange(call.insn(), stack, "stack with symbolic arguments");
    }

    @Nullable
    private static Value unknownStackChange(MethodInsnNode call, Value.Stack stack, String reason) {
        Type returnType = Type.getReturnType(call.desc);

        if (returnType.getSort() == Type.VOID) {
            stack.randomTag = true;
            return null;
        }
        if (returnType.getSort() == Type.OBJECT && returnType.getInternalName().equals(ITEM_STACK)) {
            stack.randomTag = true;
            return stack.copy();
        }

        throw new Fail(reason);
    }

    @Nullable
    private static Value onTemplates(CallContext context, Call call, Value.Stack stack, List<Value> args, int index) {
        MethodInsnNode insn = call.insn();

        if (!args.stream().filter((v) -> v != stack).allMatch(context::isConcrete)) {
            return unknownStackChange(insn, stack, "stack call with symbolic arguments");
        }

        if (args.stream().anyMatch(context::isTainted)) {
            return unknownStackChange(insn, stack, "stack call with uncertain arguments");
        }

        NumberExpr.Const constant = stack.count instanceof NumberExpr.Const c ? c : null;
        List<Object> results = new ArrayList<>();
        boolean returnsTemplate = true;

        for (ItemStack template : stack.options) {
            template.setCount(constant != null ? (int) constant.value() : 1);

            List<Value> copy = new ArrayList<>(args);

            copy.set(index, new Value.Known(template));

            Value result = context.concreteCall(new Call(insn, copy, call.loader(), call.depth()), true);

            if (constant == null && template.getCount() != 1) {
                throw new Fail("stack count changed by an unknown call");
            }
            if (result instanceof Value.Known known && known.value() != template) {
                returnsTemplate = false;
                results.add(known.value());

                if (known.value() != null && !(known.value() instanceof ItemStack)) {
                    context.stackPart(known.value(), stack);
                }
            } else if (result != null && !(result instanceof Value.Known)) {
                return unknownStackChange(insn, stack, "symbolic result of a stack call");
            }
        }

        if (constant != null && !stack.options.isEmpty()) {
            stack.count = NumberExpr.constant(stack.options.get(0).getCount());
        }
        if (Type.getReturnType(insn.desc).getSort() == Type.VOID) {
            return null;
        }
        if (returnsTemplate) {
            return stack;
        }

        results.stream().filter(Objects::nonNull).forEach(context::own);

        if (results.stream().allMatch(ItemStack.class::isInstance)) {
            return context.combine(results.stream().map(context::wrap).toList());
        }

        return results.size() == 1 ? context.wrap(results.get(0)) : new Value.Choice(results);
    }
}
