package com.yanny.ali.plugin.server.bytecode;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

public class IndyTest {
    private static final String METAFACTORY = "java/lang/invoke/LambdaMetafactory";
    private static final String CONCAT_FACTORY = "java/lang/invoke/StringConcatFactory";
    private static final Handle IMPL = new Handle(H_INVOKESTATIC, "mod/Lambdas", "lambda$0", "(Ljava/lang/String;)Ljava/lang/Object;", false);
    private static final ClassLoader LOADER = IndyTest.class.getClassLoader();

    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testMetafactoryIsClosureOverCaptured() {
        List<Value> captured = List.of(new Value.Known("x"));
        InvokeDynamicInsnNode indy = new InvokeDynamicInsnNode("get", "(Ljava/lang/String;)Ljava/util/function/Supplier;", bootstrap(METAFACTORY, "metafactory"),
                Type.getMethodType("()Ljava/lang/Object;"), IMPL, Type.getMethodType("()Ljava/lang/Object;"));

        assertEquals(new Value.Closure(Supplier.class, "get", "()Ljava/lang/Object;", IMPL, captured, LOADER), Indy.apply(context, indy, captured, LOADER));
    }

    @Test
    public void testMetafactoryWithoutImplementationFails() {
        InvokeDynamicInsnNode indy = new InvokeDynamicInsnNode("get", "()Ljava/util/function/Supplier;", bootstrap(METAFACTORY, "metafactory"),
                Type.getMethodType("()Ljava/lang/Object;"), "impl", Type.getMethodType("()Ljava/lang/Object;"));

        assertEquals("invokedynamic " + METAFACTORY, assertThrows(Fail.class, () -> Indy.apply(context, indy, List.of(), LOADER)).getMessage());
    }

    @Test
    public void testConcatOfKnownValuesIsString() {
        InvokeDynamicInsnNode indy = concat("count \u0001 of \u0002", "max");

        assertEquals(new Value.Known("count 3 of max"), Indy.apply(context, indy, List.of(new Value.Known(3)), LOADER));
    }

    @Test
    public void testConcatOfUnknownValueIsUnknown() {
        assertSame(Value.UNKNOWN, Indy.apply(context, concat("count \u0001", null), List.of(Value.UNKNOWN), LOADER));
    }

    @Test
    public void testOtherBootstrapFails() {
        InvokeDynamicInsnNode indy = new InvokeDynamicInsnNode("run", "()Ljava/lang/Runnable;", bootstrap("mod/Bootstraps", "make"));

        assertEquals("invokedynamic mod/Bootstraps", assertThrows(Fail.class, () -> Indy.apply(context, indy, List.of(), LOADER)).getMessage());
    }

    private static InvokeDynamicInsnNode concat(String recipe, String constant) {
        Handle bootstrap = bootstrap(CONCAT_FACTORY, "makeConcatWithConstants");

        if (constant == null) {
            return new InvokeDynamicInsnNode("concat", "(I)Ljava/lang/String;", bootstrap, recipe);
        }

        return new InvokeDynamicInsnNode("concat", "(I)Ljava/lang/String;", bootstrap, recipe, constant);
    }

    private static Handle bootstrap(String owner, String name) {
        return new Handle(H_INVOKESTATIC, owner, name, "()Ljava/lang/invoke/CallSite;", false);
    }
}
