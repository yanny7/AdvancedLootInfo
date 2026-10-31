package com.yanny.ali.plugin.server.bytecode;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class SelfModelTest {
    private static final String TRADER = Type.getInternalName(Trader.class);
    private static final Value.Unknown SELF = Value.Unknown.of(Type.getType(Trader.class), Origin.SELF);

    private final SelfModel model = new SelfModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testOwnMethodIsInterpreted() {
        subject();

        assertNull(model.apply(context, call(INVOKEVIRTUAL, TRADER, "level", "()I", SELF)));
        assertEquals(1, context.executions.size());
        assertEquals(Trader.class, context.executions.getFirst().owner());
        assertEquals("level", context.executions.getFirst().method().name);
        assertEquals(List.of(SELF), context.executions.getFirst().args());
    }

    @Test
    public void testSpecialCallStartsAtCalledOwner() {
        subject();

        model.apply(context, call(INVOKESPECIAL, Type.getInternalName(BaseTrader.class), "level", "()I", SELF));
        assertEquals(BaseTrader.class, context.executions.getFirst().owner());
    }

    @Test
    public void testJdkMethodHasNoOpinion() {
        subject();

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, "hashCode", "()I", SELF)));
        assertTrue(context.executions.isEmpty());
    }

    @Test
    public void testOtherReceiverHasNoOpinion() {
        subject();

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, "level", "()I", Value.UNKNOWN)));
    }

    @Test
    public void testNoSubjectHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, "level", "()I", SELF)));
    }

    private void subject() {
        context.subject = new TraderSubject(Trader.class, false);
    }

    public static class BaseTrader {
        public int level() {
            return 0;
        }
    }

    public static class Trader extends BaseTrader {
        @Override
        public int level() {
            return 1;
        }
    }
}
