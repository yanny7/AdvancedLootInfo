package com.yanny.ali.plugin.server.bytecode;

import org.junit.jupiter.api.Test;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class LoggerModelTest {
    private final LoggerModel model = new LoggerModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testVoidCallDoesNothing() {
        assertNull(model.apply(context, call(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;)V", Value.UNKNOWN, new Value.Known("text"))));
    }

    @Test
    public void testBooleanIsFalse() {
        assertEquals(new Value.Known(0), model.apply(context, call(INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "isDebugEnabled", "()Z", Value.UNKNOWN)));
    }

    @Test
    public void testOtherResultIsUnknown() {
        Value result = model.apply(context, call(INVOKEVIRTUAL, "java/util/logging/Logger", "getLevel", "()Ljava/util/logging/Level;", Value.UNKNOWN));

        assertEquals(new Value.Unknown(false, "java/util/logging/Level", null, null), result);
    }

    @Test
    public void testWideResultIsWideUnknown() {
        Value result = model.apply(context, call(INVOKEVIRTUAL, "java/util/logging/LogRecord", "getMillis", "()J", Value.UNKNOWN));

        assertEquals(new Value.Unknown(true, null, null, null), result);
    }

    @Test
    public void testOtherOwnerHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, "org/slf4jx/Logger", "info", "(Ljava/lang/String;)V", Value.UNKNOWN, new Value.Known("text"))));
    }
}
