package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

final class LoggerModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (isLogging(insn.owner)) {
            Type returnType = Type.getReturnType(insn.desc);


            if (returnType.getSort() == Type.VOID) {
                return null;
            }

            return returnType.getSort() == Type.BOOLEAN ? new Value.Known(0) : Value.Unknown.of(returnType, null);
        }

        return NO_OPINION;
    }

    private static boolean isLogging(String owner) {
        return owner.startsWith("org/slf4j/") || owner.startsWith("org/apache/logging/log4j/") || owner.startsWith("java/util/logging/");
    }
}
