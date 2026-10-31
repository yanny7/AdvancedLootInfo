package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class SelfModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        Subject subject = context.subject();
        Value receiver = insn.getOpcode() == Opcodes.INVOKESTATIC ? null : args.get(0);

        if (subject != null && subject.isSelf(receiver)) {
            boolean special = insn.getOpcode() == Opcodes.INVOKESPECIAL;
            Class<?> start = special ? load(insn.owner, call.loader()) : subject.type();
            Declaration own = ownDeclaration(start, insn.name, insn.desc, special);

            if (own != null) {
                return context.execute(own.owner(), own.method(), args, call.depth());
            }
        }

        return NO_OPINION;
    }

    @Nullable
    static Declaration ownDeclaration(Class<?> start, String name, String desc, boolean exact) {
        if (!(declaration(start, name, desc, exact) instanceof Declaration declaration) || LambdaResolver.isPlatform(Type.getInternalName(declaration.owner()))) {
            return null;
        }

        return declaration;
    }
}
