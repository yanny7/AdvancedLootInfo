package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class WorldModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.getOpcode() != Opcodes.INVOKESTATIC && call.args().get(0) instanceof Value.Unknown && RuntimeNames.is(RuntimeNames.LEVEL_IS_CLIENT_SIDE, insn)
                && Level.class.isAssignableFrom(load(insn.owner, call.loader()))) {
            return new Value.Known(0);
        }
        if (insn.getOpcode() != Opcodes.INVOKESTATIC && call.args().get(0) instanceof Value.Unknown && context.registries() != null
                && (RuntimeNames.is(RuntimeNames.REGISTRY_ACCESS, insn) || RuntimeNames.is(RuntimeNames.ENTITY_REGISTRY_ACCESS, insn))) {
            return new Value.Known(context.registries());
        }

        return NO_OPINION;
    }
}
