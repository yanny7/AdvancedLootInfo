package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.CONSTRUCTOR;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.num;

final class ItemCostModel implements MethodModel {
    static final String ITEM_COST = Type.getInternalName(ItemCost.class);

    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.name.equals(CONSTRUCTOR)) {
            if (insn.owner.equals(ITEM_COST)) {
                context.traceCase("constructor.itemCost", insn);
                return ItemStackModel.construct(context, ItemCost.class, insn.desc, call.args());
            }

            return NO_OPINION;
        }

        Value receiver = insn.getOpcode() == Opcodes.INVOKESTATIC ? null : call.args().get(0);

        if (receiver instanceof Value.Stack stack && insn.owner.equals(ITEM_COST)) {
            context.traceCase("receiver.cost", insn);
            return costCall(context, call, stack);
        }

        return NO_OPINION;
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

            copy.set(0, new Value.Known(new ItemCost(template.getItemHolder(), 1, DataComponentPredicate.EMPTY)));

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
