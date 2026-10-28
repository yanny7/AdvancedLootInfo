package com.yanny.ali.plugin.server.bytecode;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

public class MapModelTest {
    private static final String MAP = "java/util/Map";
    private static final Origin ORIGIN = new Origin(null, "getVariant()Ljava/lang/Object;", List.of());
    private static final Value.Unknown KEY = new Value.Unknown(false, "java/lang/Object", null, ORIGIN);

    private final MapModel model = new MapModel();
    private final FakeCallContext context = new FakeCallContext();
    private final Map<String, Integer> map = new TreeMap<>(Map.of("b", 2, "a", 1, "c", 3));

    @Test
    public void testKeysAreDecidedInTextOrder() {
        assertEquals(Interpreter.NULL, model.apply(context, get(KEY)));
        assertEquals(List.of("a", "b", "c"), context.decisions.stream().map(Decision::value).toList());
        assertTrue(context.decisions.stream().allMatch((d) -> d.kind() == Decision.Kind.EQUALS && d.subject() == ORIGIN));
    }

    @Test
    public void testHeldKeyIsJdkCallWithThatKey() {
        context.answers.addAll(List.of(false, true));
        context.jdkCall = (call) -> new Value.Known(2);

        assertEquals(new Value.Known(2), model.apply(context, get(KEY)));
        assertEquals(1, context.jdkCalls.size());
        assertEquals(new Value.Known("b"), context.jdkCalls.get(0).args().get(1));
        assertEquals(2, context.decisions.size());
    }

    @Test
    public void testMissingKeyOfGetOrDefaultIsDefault() {
        Value fallback = new Value.Known(7);

        assertSame(fallback, model.apply(context, call(INVOKEINTERFACE, MAP, "getOrDefault", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", new Value.Known(map), KEY, fallback)));
    }

    @Test
    public void testMissingKeyOfContainsKeyIsFalse() {
        assertEquals(new Value.Known(0), model.apply(context, call(INVOKEINTERFACE, MAP, "containsKey", "(Ljava/lang/Object;)Z", new Value.Known(map), KEY)));
    }

    @Test
    public void testConcreteKeyHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, get(new Value.Known("a"))));
    }

    @Test
    public void testUnknownKeyWithoutOriginHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, get(Value.UNKNOWN)));
    }

    @Test
    public void testOtherMethodHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEINTERFACE, MAP, "remove", "(Ljava/lang/Object;)Ljava/lang/Object;", new Value.Known(map), KEY)));
    }

    private Call get(Value key) {
        return call(INVOKEINTERFACE, MAP, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", new Value.Known(map), key);
    }
}
