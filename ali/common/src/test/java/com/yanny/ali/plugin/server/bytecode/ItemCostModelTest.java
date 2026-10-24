package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class ItemCostModelTest {
    private static final String ITEM_COST = "net/minecraft/world/item/trading/ItemCost";
    private static final String WITH_COMPONENTS = "(Ljava/util/function/UnaryOperator;)Lnet/minecraft/world/item/trading/ItemCost;";
    private static final Value.Num ONE_TO_THREE = new Value.Num(NumberExpr.uniformInt(1, 3), Value.Kind.INT);

    private final ItemCostModel model = new ItemCostModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testCanonicalConstructorIsShownStackWithItsCount() {
        Value.Stack shown = stack(Items.EMERALD, 1);
        Value result = model.apply(context, construct("(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/DataComponentPredicate;Lnet/minecraft/world/item/ItemStack;)V",
                new Value.Known(Items.EMERALD.builtInRegistryHolder()), ONE_TO_THREE, new Value.Known(DataComponentPredicate.EMPTY), shown));
        Value.Stack cost = assertInstanceOf(Value.Stack.class, result);

        assertNotSame(shown, cost);
        assertTrue(cost.options.get(0).is(Items.EMERALD));
        assertEquals(ONE_TO_THREE.expr(), cost.count);
    }

    @Test
    public void testConstructorOfItemIsStack() {
        Value.Stack cost = assertInstanceOf(Value.Stack.class, model.apply(context, construct("(Lnet/minecraft/world/level/ItemLike;I)V", new Value.Known(Items.EMERALD),
                new Value.Known(3))));

        assertTrue(cost.options.get(0).is(Items.EMERALD));
        assertEquals(NumberExpr.constant(3), cost.count);
    }

    @Test
    public void testItemStackAndCountOfCost() {
        Value.Stack cost = stack(Items.EMERALD, 3);

        assertSame(cost, model.apply(context, costCall(RuntimeNames.COST_ITEM_STACK, cost)));
        assertEquals(new Value.Known(3), model.apply(context, costCall(RuntimeNames.COST_COUNT, cost)));
    }

    @Test
    public void testCallRunsOnEachTemplate() {
        Value.Stack cost = stack(Items.EMERALD, 3);

        context.concreteCall = (call) -> new Value.Known(new ItemCost(Items.DIAMOND, 1));

        Value.Stack changed = assertInstanceOf(Value.Stack.class, model.apply(context, call(INVOKEVIRTUAL, ITEM_COST, "withComponents", WITH_COMPONENTS, cost,
                new Value.Known(null))));

        assertTrue(changed.options.get(0).is(Items.DIAMOND));
        assertEquals(NumberExpr.constant(3), changed.count);
        assertTrue(((ItemCost) ((Value.Known) context.concreteCalls.get(0).args().get(0)).value()).itemStack().is(Items.EMERALD));
    }

    @Test
    public void testCallWithSymbolicArgumentReturnsMarkedCopy() {
        Value.Stack cost = stack(Items.EMERALD, 3);
        Value.Stack changed = assertInstanceOf(Value.Stack.class, model.apply(context, call(INVOKEVIRTUAL, ITEM_COST, "withComponents", WITH_COMPONENTS, cost, ONE_TO_THREE)));

        assertNotSame(cost, changed);
        assertTrue(changed.randomTag);
        assertFalse(cost.randomTag);
    }

    @Test
    public void testCallReturningOtherTypeFails() {
        Call call = call(INVOKEVIRTUAL, ITEM_COST, "test", "(Lnet/minecraft/world/item/ItemStack;)Z", stack(Items.EMERALD, 3), new Value.Known(ItemStack.EMPTY));

        assertEquals("item cost call test", assertThrows(Fail.class, () -> model.apply(context, call)).getMessage());
    }

    @Test
    public void testOtherCallHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, "net/minecraft/world/item/ItemStack", "getCount", "()I", stack(Items.EMERALD, 3))));
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKESPECIAL, "java/util/ArrayList", Interpreter.CONSTRUCTOR, "()V")));
    }

    private static Value.Stack stack(net.minecraft.world.item.Item item, int count) {
        return new Value.Stack(List.of(new ItemStack(item)), NumberExpr.constant(count), false);
    }

    private static Call costCall(RuntimeNames.Ref ref, Value... args) {
        return call(INVOKEVIRTUAL, ITEM_COST, ref.name(), ref.desc(), args);
    }

    private static Call construct(String desc, Value... args) {
        return call(INVOKESPECIAL, ITEM_COST, Interpreter.CONSTRUCTOR, desc, args);
    }
}
