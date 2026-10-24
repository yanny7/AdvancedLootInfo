package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import org.junit.jupiter.api.Test;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class NumberModelTest {
    private static final NumberExpr DRAW = NumberExpr.uniformInt(0, 4);
    private static final Value.Num INT = new Value.Num(DRAW, Value.Kind.INT);

    private final NumberModel model = new NumberModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testUnboxKeepsExpressionWithKind() {
        assertEquals(INT, model.apply(context, call(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", INT)));
        assertEquals(new Value.Num(DRAW, Value.Kind.DOUBLE), model.apply(context, call(INVOKEVIRTUAL, "java/lang/Integer", "doubleValue", "()D", INT)));
    }

    @Test
    public void testOtherNumberMethodFails() {
        assertEquals("number method compareTo", assertThrows(Fail.class,
                () -> model.apply(context, call(INVOKEVIRTUAL, "java/lang/Integer", "compareTo", "(Ljava/lang/Integer;)I", INT, new Value.Known(1)))).getMessage());
    }

    @Test
    public void testKnownReceiverHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", new Value.Known(3))));
    }

    @Test
    public void testMathMinOfDrawIsMinExpression() {
        Value result = NumberModel.STATICS.apply(context, call(INVOKESTATIC, "java/lang/Math", "min", "(II)I", INT, new Value.Known(2)));

        assertEquals(Interpreter.num(NumberExpr.min(DRAW, NumberExpr.constant(2)), Value.Kind.INT), result);
    }

    @Test
    public void testStrictMathFloorTakesReturnKind() {
        Value result = NumberModel.STATICS.apply(context, call(INVOKESTATIC, "java/lang/StrictMath", "floor", "(D)D", INT));

        assertEquals(Interpreter.num(NumberExpr.fn(NumberFunctions.FLOOR, DRAW), Value.Kind.DOUBLE), result);
    }

    @Test
    public void testUnsupportedMathFails() {
        assertEquals("Math.sin", assertThrows(Fail.class, () -> NumberModel.STATICS.apply(context, call(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", INT))).getMessage());
    }

    @Test
    public void testMathOfConcreteHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, NumberModel.STATICS.apply(context, call(INVOKESTATIC, "java/lang/Math", "min", "(II)I", new Value.Known(1), new Value.Known(2))));
    }

    @Test
    public void testValueOfIsSameNumber() {
        assertSame(INT, NumberModel.STATICS.apply(context, call(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", INT)));
    }

    @Test
    public void testStringValueOfIsUnknown() {
        assertSame(Value.UNKNOWN, NumberModel.STATICS.apply(context, call(INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", INT)));
    }

    @Test
    public void testInstanceCallHasNoStaticOpinion() {
        assertSame(MethodModel.NO_OPINION, NumberModel.STATICS.apply(context, call(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", INT)));
    }
}
