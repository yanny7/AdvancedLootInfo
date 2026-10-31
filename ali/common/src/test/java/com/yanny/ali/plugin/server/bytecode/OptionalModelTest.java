package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.closure;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class OptionalModelTest {
    private static final String OPTIONAL = "java/util/Optional";

    private final OptionalModel model = new OptionalModel();
    private final FakeCallContext context = new FakeCallContext();
    private final Value.Stack stack = new Value.Stack(List.of(new ItemStack(Items.EMERALD)), NumberExpr.constant(1), false);

    @Test
    public void testOfStackIsStack() {
        assertSame(stack, model.apply(context, call(INVOKESTATIC, OPTIONAL, "of", "(Ljava/lang/Object;)Ljava/util/Optional;", stack)));
        assertSame(stack, model.apply(context, call(INVOKESTATIC, OPTIONAL, "ofNullable", "(Ljava/lang/Object;)Ljava/util/Optional;", stack)));
    }

    @Test
    public void testOfConcreteHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKESTATIC, OPTIONAL, "of", "(Ljava/lang/Object;)Ljava/util/Optional;", new Value.Known("a"))));
    }

    @Test
    public void testOtherOwnerHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKESTATIC, "java/util/List", "of", "(Ljava/lang/Object;)Ljava/util/List;", stack)));
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", stack)));
    }

    @Test
    public void testStackIsPresent() {
        assertEquals(new Value.Known(1), model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "isPresent", "()Z", stack)));
        assertEquals(new Value.Known(0), model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "isEmpty", "()Z", stack)));
        assertSame(stack, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "get", "()Ljava/lang/Object;", stack)));
        assertSame(stack, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "orElseThrow", "()Ljava/lang/Object;", stack)));
        assertSame(stack, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", stack, new Value.Known(null))));
    }

    @Test
    public void testStackOrElseGetRunsNoLambda() {
        assertSame(stack, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "orElseGet", "(Ljava/util/function/Supplier;)Ljava/lang/Object;", stack, closure())));
        assertTrue(context.invocations.isEmpty());
    }

    @Test
    public void testStackIfPresentRunsLambdaOnStack() {
        Value.Closure consumer = closure();

        assertNull(model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "ifPresent", "(Ljava/util/function/Consumer;)V", stack, consumer)));
        assertEquals(List.of(new FakeCallContext.Invocation(consumer, List.of(stack), 3)), context.invocations);
    }

    @Test
    public void testStackMapToStackIsStack() {
        Value.Stack mapped = stack.copy();

        context.invokeResult = mapped;
        assertSame(mapped, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "map", "(Ljava/util/function/Function;)Ljava/util/Optional;", stack, closure())));
    }

    @Test
    public void testStackMapToConcreteIsOptional() {
        context.invokeResult = new Value.Known("a");
        assertEquals(new Value.Known(Optional.of("a")), model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "map", "(Ljava/util/function/Function;)Ljava/util/Optional;", stack, closure())));

        context.invokeResult = new Value.Known(null);
        assertEquals(new Value.Known(Optional.empty()), model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "map", "(Ljava/util/function/Function;)Ljava/util/Optional;", stack, closure())));
    }

    @Test
    public void testStackMapToSymbolicFails() {
        context.invokeResult = Value.UNKNOWN;
        assertFail("symbolic result of Optional.map", call(INVOKEVIRTUAL, OPTIONAL, "map", "(Ljava/util/function/Function;)Ljava/util/Optional;", stack, closure()));
    }

    @Test
    public void testStackMapWithoutLambdaFails() {
        assertFail("symbolic Optional function", call(INVOKEVIRTUAL, OPTIONAL, "map", "(Ljava/util/function/Function;)Ljava/util/Optional;", stack, Value.UNKNOWN));
    }

    @Test
    public void testStackOtherCallFails() {
        assertFail("symbolic Optional.filter", call(INVOKEVIRTUAL, OPTIONAL, "filter", "(Ljava/util/function/Predicate;)Ljava/util/Optional;", stack, closure()));
    }

    @Test
    public void testConcreteWithoutSymbolicArgumentHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "isPresent", "()Z", new Value.Known(Optional.of("a")))));
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", new Value.Known(Optional.empty()), new Value.Known("b"))));
    }

    @Test
    public void testConcreteOrElseWithSymbolicArgument() {
        Value.Unknown other = new Value.Unknown(false, "java/lang/String", null, Origin.SELF);

        assertEquals(new Value.Known("a"), model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", new Value.Known(Optional.of("a")), other)));
        assertSame(other, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", new Value.Known(Optional.empty()), other)));
    }

    @Test
    public void testConcreteEmptyOrElseGetRunsLambda() {
        Value.Closure supplier = closure();

        context.invokeResult = stack;
        assertSame(stack, model.apply(context, call(INVOKEVIRTUAL, OPTIONAL, "orElseGet", "(Ljava/util/function/Supplier;)Ljava/lang/Object;", new Value.Known(Optional.empty()), supplier)));
        assertEquals(List.of(new FakeCallContext.Invocation(supplier, List.of(), 3)), context.invocations);
    }

    @Test
    public void testConcreteEmptyOrElseThrowFails() {
        assertFail("value of an empty Optional", call(INVOKEVIRTUAL, OPTIONAL, "orElseThrow", "(Ljava/util/function/Supplier;)Ljava/lang/Object;", new Value.Known(Optional.empty()), closure()));
    }

    private void assertFail(String message, Call call) {
        assertEquals(message, assertThrows(Fail.class, () -> model.apply(context, call)).getMessage());
    }
}
