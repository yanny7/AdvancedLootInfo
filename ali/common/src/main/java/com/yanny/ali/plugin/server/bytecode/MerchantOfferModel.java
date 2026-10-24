package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.server.bytecode.Arithmetic.floatOf;
import static com.yanny.ali.plugin.server.bytecode.Arithmetic.intOf;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.CONSTRUCTOR;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.ITEM_STACK;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.MERCHANT_OFFER;

final class MerchantOfferModel implements MethodModel, Opcodes {
    private static final String OPTIONAL = Type.getInternalName(Optional.class);

    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.name.equals(CONSTRUCTOR) && (insn.owner.equals(MERCHANT_OFFER)
                || !call.args().stream().allMatch(context::isConcrete) && isForwardingOffer(Resolution.load(insn.owner, call.loader()), insn.desc))) {
            context.traceCase("constructor.offer", insn);
            return construct(context, insn.desc, call.args());
        }

        return NO_OPINION;
    }

    private static Value construct(CallContext context, String desc, List<Value> args) {
        Type[] params = Type.getArgumentTypes(desc);

        if (params.length >= 5 && TradeTypes.isObject(params[0], ItemCostModel.ITEM_COST)) {
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
        if (args.stream().allMatch(context::isConcrete)) {
            Constructor<?> constructor = Resolution.constructor(MerchantOffer.class, desc);

            try {
                return context.wrap(constructor.newInstance(context.javaArgs(args, constructor.getParameterTypes(), 0)));
            } catch (Throwable e) {
                throw new Fail("offer constructor failed");
            }
        }

        throw new Fail("offer constructor");
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

    private static boolean isForwardingOffer(Class<?> type, String desc) {
        ClassNode node = type != MerchantOffer.class && MerchantOffer.class.isAssignableFrom(type) ? Resolution.classNode(type) : null;
        MethodNode init = node != null ? Resolution.findMethod(node, CONSTRUCTOR, desc) : null;

        if (init == null) {
            return false;
        }

        List<AbstractInsnNode> insns = Arrays.stream(init.instructions.toArray()).filter((i) -> i.getOpcode() >= 0).toList();
        Type[] params = Type.getArgumentTypes(desc);

        if (insns.size() != params.length + 3 || !(insns.get(0) instanceof VarInsnNode self) || self.getOpcode() != ALOAD || self.var != 0) {
            return false;
        }

        int slot = 1;

        for (int i = 0; i < params.length; i++) {
            if (!(insns.get(i + 1) instanceof VarInsnNode load) || load.getOpcode() != params[i].getOpcode(ILOAD) || load.var != slot) {
                return false;
            }

            slot += params[i].getSize();
        }

        return insns.get(params.length + 1) instanceof MethodInsnNode call && call.getOpcode() == INVOKESPECIAL && call.owner.equals(MERCHANT_OFFER)
                && call.name.equals(CONSTRUCTOR) && call.desc.equals(desc) && insns.get(params.length + 2).getOpcode() == RETURN;
    }
}
