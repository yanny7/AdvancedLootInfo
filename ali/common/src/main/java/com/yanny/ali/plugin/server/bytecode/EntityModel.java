package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class EntityModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        EntitySubject subject = (EntitySubject) context.subject();

        if (insn.getOpcode() == Opcodes.INVOKESTATIC || subject == null || !subject.isSelf(call.args().getFirst())) {
            return NO_OPINION;
        }
        if (RuntimeNames.is(RuntimeNames.ENTITY_GET_TYPE, insn)) {
            return context.wrap(subject.entityType());
        }
        if (RuntimeNames.is(RuntimeNames.GET_DEFAULT_LOOT_TABLE, insn) || RuntimeNames.is(RuntimeNames.MOB_GET_LOOT_TABLE, insn)) {
            Class<?> start = insn.getOpcode() == Opcodes.INVOKESPECIAL ? load(insn.owner, call.loader()) : subject.type();
            Declaration own = SelfModel.ownDeclaration(start, RuntimeNames.GET_DEFAULT_LOOT_TABLE.name(), RuntimeNames.GET_DEFAULT_LOOT_TABLE.desc(), false);

            if (own != null) {
                return context.execute(own.owner(), own.method(), List.of(call.args().getFirst()), call.depth());
            }

            return context.wrap(subject.entityType().getDefaultLootTable());
        }

        return NO_OPINION;
    }
}
