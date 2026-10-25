package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.server.bytecode.Arithmetic.floatOf;
import static com.yanny.ali.plugin.server.bytecode.Arithmetic.intOf;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.CONSTRUCTOR;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.ITEM_STACK;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.num;

final class ItemCostModel implements MethodModel {
    static final String ITEM_COST = Type.getInternalName(ItemCost.class);
    private static final String OPTIONAL = Type.getInternalName(Optional.class);

    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.name.equals(CONSTRUCTOR)) {
            if (insn.owner.equals(ITEM_COST)) {
                return construct(context, insn.desc, call.args());
            }

            return NO_OPINION;
        }

        Value receiver = insn.getOpcode() == Opcodes.INVOKESTATIC ? null : call.args().get(0);

        if (receiver instanceof Value.Stack stack && insn.owner.equals(ITEM_COST)) {
            return costCall(context, call, stack);
        }

        return NO_OPINION;
    }

    @Nullable
    static Value.Offer offer(Type[] params, List<Value> args) {
        if (params.length >= 5 && TradeTypes.isObject(params[0], ITEM_COST)) {
            boolean hasCostB = TradeTypes.isObject(params[1], OPTIONAL);
            int resultIndex = hasCostB ? 2 : 1;
            List<Value> numbers = args.subList(resultIndex + 1, args.size());
            Value costA = cost(args.get(0));
            Value costB = hasCostB ? optionalCost(args.get(1)) : new Value.Known(ItemStack.EMPTY);
            Value result = args.get(resultIndex);

            if (TradeTypes.isObject(params[resultIndex], ITEM_STACK) && numbers.size() == 3) {
                return new Value.Offer(costA, costB, result, intOf(numbers.get(0)), intOf(numbers.get(1)), floatOf(numbers.get(2)));
            }
            if (TradeTypes.isObject(params[resultIndex], ITEM_STACK) && numbers.size() >= 4) {
                return new Value.Offer(costA, costB, result, intOf(numbers.get(1)), intOf(numbers.get(2)), floatOf(numbers.get(3)));
            }
        }

        return null;
    }

    private static Value construct(CallContext context, String desc, List<Value> args) {
        if (Type.getArgumentTypes(desc).length == 4 && args.get(3) instanceof Value.Stack shown) {
            Value.Stack cost = shown.copy();

            cost.count = context.expr(args.get(1));
            return cost;
        }

        return ItemStackModel.construct(context, ItemCost.class, desc, args);
    }

    private static Value cost(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof ItemCost cost) {
            return new Value.Known(cost.itemStack());
        }
        if (value instanceof Value.Choice choice && choice.options().stream().allMatch(ItemCost.class::isInstance)) {
            return new Value.Choice(choice.options().stream().<Object>map((o) -> ((ItemCost) o).itemStack()).toList(), choice.draw());
        }

        return value;
    }

    private static Value optionalCost(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Optional<?> optional) {
            return optional.isPresent() ? cost(new Value.Known(optional.get())) : new Value.Known(ItemStack.EMPTY);
        }

        return cost(value);
    }

    @Nullable
    private static Value costCall(CallContext context, Call call, Value.Stack stack) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();

        if (RuntimeNames.is(RuntimeNames.COST_ITEM_STACK, insn)) {
            return stack;
        } else if (RuntimeNames.is(RuntimeNames.COST_COUNT, insn)) {
            return num(stack.count, Value.Kind.INT);
        } else if (!TradeTypes.isObject(Type.getReturnType(insn.desc), ITEM_COST)) {
            throw new Fail("item cost call " + insn.name);
        } else if (!args.stream().skip(1).allMatch(context::isConcrete) || args.stream().anyMatch(context::isTainted)) {
            return ItemStackModel.unknownStackChange(insn, stack, "item cost call with symbolic arguments");
        }

        List<ItemStack> templates = new ArrayList<>();

        for (ItemStack template : stack.options) {
            List<Value> copy = new ArrayList<>(args);

            copy.set(0, new Value.Known(new ItemCost(template.getItemHolder(), 1, DataComponentExactPredicate.EMPTY)));

            if (!(context.concreteCall(new Call(insn, copy, call.loader(), call.depth()), true) instanceof Value.Known known) || !(known.value() instanceof ItemCost cost)) {
                throw new Fail("item cost call " + insn.name);
            }

            templates.add(cost.itemStack());
        }

        Value.Stack result = new Value.Stack(templates, stack.count, stack.randomTag);

        result.destination = stack.destination;
        result.decoration = stack.decoration;
        return result;
    }
}
