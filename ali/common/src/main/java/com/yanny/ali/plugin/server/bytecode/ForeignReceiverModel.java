package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class ForeignReceiverModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        Subject subject = context.subject();

        if (insn.getOpcode() == Opcodes.INVOKESTATIC || !(args.getFirst() instanceof Value.Unknown receiver) || subject != null && subject.isSelf(receiver)
                || LambdaResolver.isPlatform(insn.owner)) {
            return NO_OPINION;
        }

        Class<?> owner;

        try {
            owner = load(insn.owner, call.loader());
        } catch (Fail fail) {
            return NO_OPINION;
        }

        if (!(declaration(owner, insn.name, insn.desc, insn.getOpcode() == Opcodes.INVOKESPECIAL) instanceof Declaration(Class<?> declaring, MethodNode method))
                || LambdaResolver.isPlatform(Type.getInternalName(declaring))) {
            return NO_OPINION;
        }

        try {
            return context.execute(declaring, method, args, call.depth());
        } catch (Fail fail) {
            if (fail.isBudget()) {
                throw fail;
            }

            Type returnType = Type.getReturnType(insn.desc);

            return returnType.getSort() == Type.VOID ? null : Value.Unknown.of(returnType, Origin.of(receiver, insn.name + insn.desc, args.subList(1, args.size())));
        }
    }
}
