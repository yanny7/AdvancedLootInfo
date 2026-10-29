package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class RandomModelTest {
    private static final String RANDOM_SOURCE = "net/minecraft/util/RandomSource";
    private static final String RANDOM = "java/util/Random";
    private static final Value.Rng SOURCE = new Value.Rng(false);
    private static final Value.Rng LEGACY = new Value.Rng(true);

    private final RandomModel model = new RandomModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testBoundIsUniformDrawBelowBound() {
        NumberExpr expected = NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.sub(NumberExpr.constant(5), NumberExpr.constant(1)));

        assertEquals(new Value.Num(expected, Value.Kind.INT), model.apply(context, sourceCall(RuntimeNames.NEXT_INT_BOUND, SOURCE, new Value.Known(5))));
        assertEquals(List.of(expected), context.draws);
    }

    @Test
    public void testBetweenIsUniformDrawInclusive() {
        NumberExpr expected = NumberExpr.uniformInt(NumberExpr.constant(2), NumberExpr.constant(4));

        assertEquals(new Value.Num(expected, Value.Kind.INT), model.apply(context, sourceCall(RuntimeNames.NEXT_INT_BETWEEN, SOURCE, new Value.Known(2), new Value.Known(4))));
    }

    @Test
    public void testFloatIsUniformFraction() {
        assertEquals(new Value.Num(NumberExpr.uniformFloat(0, 1), Value.Kind.FLOAT), model.apply(context, sourceCall(RuntimeNames.NEXT_FLOAT, SOURCE)));
    }

    @Test
    public void testLegacyOriginExcludesBound() {
        NumberExpr expected = NumberExpr.uniformInt(NumberExpr.constant(2), NumberExpr.sub(NumberExpr.constant(5), NumberExpr.constant(1)));

        assertEquals(new Value.Num(expected, Value.Kind.INT), model.apply(context, call(INVOKEVIRTUAL, RANDOM, "nextInt", "(II)I", LEGACY, new Value.Known(2), new Value.Known(5))));
    }

    @Test
    public void testUnboundedIntFails() {
        assertEquals("unbounded random int", assertThrows(Fail.class, () -> model.apply(context, call(INVOKEVIRTUAL, RANDOM, "nextInt", "()I", LEGACY))).getMessage());
    }

    @Test
    public void testForkIsSameSource() {
        assertSame(SOURCE, model.apply(context, sourceCall(RuntimeNames.FORK, SOURCE)));
    }

    @Test
    public void testSetSeedDoesNothing() {
        assertNull(model.apply(context, call(INVOKEVIRTUAL, RANDOM, "setSeed", "(J)V", LEGACY, new Value.Known(1L))));
        assertTrue(context.draws.isEmpty());
    }

    @Test
    public void testStubModeDrawsStubValue() {
        context.mode = Interpreter.Mode.STUB;
        context.pick = 1;

        assertEquals(new Value.Known(StubRandom.bound(1, 5)), model.apply(context, sourceCall(RuntimeNames.NEXT_INT_BOUND, SOURCE, new Value.Known(5))));
        assertEquals(new Value.Known(1), model.apply(context, sourceCall(RuntimeNames.NEXT_BOOLEAN, SOURCE)));
        assertTrue(context.draws.isEmpty());
    }

    @Test
    public void testOtherSourceMethodRunsItsDeclaration() {
        Call call = call(INVOKEINTERFACE, RANDOM_SOURCE, "triangle", "(DD)D", SOURCE, new Value.Known(0D), new Value.Known(1D));

        context.executeResult = new Value.Known(0.5D);
        assertEquals(new Value.Known(0.5D), model.apply(context, call));
        assertEquals(1, context.executions.size());
        assertSame(RandomSource.class, context.executions.getFirst().owner());
        assertEquals("triangle", context.executions.getFirst().method().name);
        assertEquals(call.depth(), context.executions.getFirst().depth());
    }

    @Test
    public void testOtherLegacyMethodFails() {
        assertEquals("random method ints", assertThrows(Fail.class, () -> model.apply(context, call(INVOKEVIRTUAL, RANDOM, "ints", "()Ljava/util/stream/IntStream;", LEGACY))).getMessage());
    }

    @Test
    public void testOtherReceiverHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, RANDOM, "nextInt", "(I)I", new Value.Known(null), new Value.Known(5))));
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKESTATIC, "mod/Util", "roll", "(Lnet/minecraft/util/RandomSource;)I", SOURCE)));
    }

    private static Call sourceCall(RuntimeNames.Ref ref, Value... args) {
        return call(INVOKEINTERFACE, RANDOM_SOURCE, ref.name(), ref.desc(), args);
    }
}
