package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

final class WorldModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.getOpcode() != Opcodes.INVOKESTATIC && call.args().get(0) instanceof Value.Unknown && context.registries() != null
                && (RuntimeNames.is(RuntimeNames.REGISTRY_ACCESS, insn) || RuntimeNames.is(RuntimeNames.ENTITY_REGISTRY_ACCESS, insn))) {
            context.traceCase("receiver.registryAccess", insn);
            return new Value.Known(context.registries());
        }

        return NO_OPINION;
    }
}
