package com.yanny.ali.plugin.server.bytecode;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.ITEM_STACK;

final class ComponentModel {
    private ComponentModel() {}

    static Value unknownChange(MethodInsnNode call, Value.Stack stack) {
        Type returnType = Type.getReturnType(call.desc);

        if (TradeTypes.isObject(returnType, ItemCostModel.ITEM_COST)) {
            Value.Stack copy = stack.copy();

            copy.randomTag = true;
            return copy;
        }
        if (isComponentWrite(call)) {
            stack.randomTag = true;
            return Value.Unknown.of(returnType, null);
        }

        return MethodModel.NO_OPINION;
    }

    private static boolean isComponentWrite(MethodInsnNode call) {
        if (call.owner.equals(ITEM_STACK) && RuntimeNames.STACK_SET != null && RuntimeNames.STACK_UPDATE != null) {
            return call.name.equals(RuntimeNames.STACK_SET.name()) || call.name.equals(RuntimeNames.STACK_UPDATE.name());
        }

        return RuntimeNames.is(RuntimeNames.UPDATE_ENCHANTMENTS, call);
    }
}
