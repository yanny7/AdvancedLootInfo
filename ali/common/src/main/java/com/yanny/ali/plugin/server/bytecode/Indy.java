package com.yanny.ali.plugin.server.bytecode;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class Indy {
    private static final String LAMBDA_METAFACTORY = "java/lang/invoke/LambdaMetafactory";
    private static final String STRING_CONCAT_FACTORY = "java/lang/invoke/StringConcatFactory";

    private Indy() {}

    static Value apply(CallContext context, InvokeDynamicInsnNode indy, List<Value> captured, ClassLoader loader) {
        return switch (indy.bsm.getOwner()) {
            case LAMBDA_METAFACTORY -> lambda(indy, captured, loader);
            case STRING_CONCAT_FACTORY -> concat(context, indy, captured);
            default -> throw unsupported(indy);
        };
    }

    private static Value lambda(InvokeDynamicInsnNode indy, List<Value> captured, ClassLoader loader) {
        if (indy.bsmArgs.length < 3 || !(indy.bsmArgs[1] instanceof Handle impl)) {
            throw unsupported(indy);
        }

        Class<?> iface = load(Type.getReturnType(indy.desc).getInternalName(), loader);

        return new Value.Closure(iface, indy.name, ((Type) indy.bsmArgs[0]).getDescriptor(), impl, captured, loader);
    }

    private static Value concat(CallContext context, InvokeDynamicInsnNode indy, List<Value> captured) {
        if (!indy.bsm.getName().equals("makeConcatWithConstants")) {
            throw unsupported(indy);
        }
        if (!captured.stream().allMatch((v) -> v instanceof Value.Known)) {
            return Value.UNKNOWN;
        }

        String recipe = (String) indy.bsmArgs[0];
        Type[] types = Type.getArgumentTypes(indy.desc);
        StringBuilder builder = new StringBuilder();
        int arg = 0;
        int constant = 1;

        for (char c : recipe.toCharArray()) {
            if (c == '\u0001') {
                builder.append(context.toJava(captured.get(arg), Machine.javaType(types[arg])));
                arg++;
            } else if (c == '\u0002') {
                builder.append(indy.bsmArgs[constant++]);
            } else {
                builder.append(c);
            }
        }

        return new Value.Known(builder.toString());
    }

    private static Fail unsupported(InvokeDynamicInsnNode indy) {
        return new Fail("invokedynamic " + indy.bsm.getOwner());
    }
}
