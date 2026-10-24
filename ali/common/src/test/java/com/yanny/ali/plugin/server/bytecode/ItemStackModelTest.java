package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class ItemStackModelTest {
    private static final String ITEM_STACK = "net/minecraft/world/item/ItemStack";
    private static final String ENCHANT = "(Lnet/minecraft/world/item/enchantment/Enchantment;I)V";
    private static final String HOVER_NAME = "(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/world/item/ItemStack;";
    private static final String TRADE = "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/trading/MerchantOffer;";
    private static final Value.Num ONE_TO_THREE = new Value.Num(NumberExpr.uniformInt(1, 3), Value.Kind.INT);

    private final ItemStackModel model = new ItemStackModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testConstructorOfItemIsStack() {
        Value.Stack stack = assertInstanceOf(Value.Stack.class, model.apply(context, construct(ITEM_STACK, "(Lnet/minecraft/world/level/ItemLike;I)V", new Value.Known(Items.EMERALD),
                new Value.Known(3))));

        assertEquals(1, stack.options.size());
        assertTrue(stack.options.get(0).is(Items.EMERALD));
        assertEquals(NumberExpr.constant(3), stack.count);
        assertFalse(stack.randomTag);
    }

    @Test
    public void testConstructorOfChoiceIsStackOfEachItem() {
        Value.Stack stack = assertInstanceOf(Value.Stack.class, model.apply(context, construct(ITEM_STACK, "(Lnet/minecraft/world/level/ItemLike;I)V",
                new Value.Choice(List.of(Items.EMERALD, Items.DIAMOND)), ONE_TO_THREE)));

        assertEquals(List.of(Items.EMERALD, Items.DIAMOND), stack.options.stream().map(ItemStack::getItem).toList());
        assertEquals(List.of(1, 1), stack.options.stream().map(ItemStack::getCount).toList());
        assertEquals(ONE_TO_THREE.expr(), stack.count);
    }

    @Test
    public void testConstructorOfNullItemFails() {
        Call call = construct(ITEM_STACK, "(Lnet/minecraft/world/level/ItemLike;)V", new Value.Known(null));

        assertEquals("null item", assertThrows(Fail.class, () -> model.apply(context, call)).getMessage());
    }

    @Test
    public void testOtherConstructorHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, construct("java/util/ArrayList", "(Ljava/lang/Object;)V", stack(Items.EMERALD, 1))));
        assertSame(MethodModel.NO_OPINION, model.apply(context, construct("java/util/ArrayList", "()V")));
    }

    @Test
    public void testSetCountWritesCount() {
        Value.Stack stack = stack(Items.EMERALD, 1);

        assertNull(model.apply(context, stackCall(RuntimeNames.STACK_SET_COUNT, stack, ONE_TO_THREE)));
        assertEquals(ONE_TO_THREE.expr(), stack.count);
    }

    @Test
    public void testGrowAddsToCount() {
        Value.Stack stack = stack(Items.EMERALD, 1);

        assertNull(model.apply(context, stackCall(RuntimeNames.STACK_GROW, stack, ONE_TO_THREE)));
        assertEquals(NumberExpr.add(NumberExpr.constant(1), ONE_TO_THREE.expr()), stack.count);
    }

    @Test
    public void testGetCountOfConstantIsKnown() {
        assertEquals(new Value.Known(2), model.apply(context, stackCall(RuntimeNames.STACK_GET_COUNT, stack(Items.EMERALD, 2))));
    }

    @Test
    public void testGetItemOfSeveralItemsIsChoice() {
        Value.Stack stack = new Value.Stack(List.of(new ItemStack(Items.EMERALD), new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD)), NumberExpr.constant(1), false);

        assertEquals(new Value.Choice(List.of(Items.EMERALD, Items.DIAMOND)), model.apply(context, stackCall(RuntimeNames.STACK_GET_ITEM, stack)));
    }

    @Test
    public void testIsEmptyDependsOnCountBounds() {
        Value.Stack random = stack(Items.EMERALD, 1);

        random.count = NumberExpr.uniformInt(0, 2);
        assertEquals(new Value.Known(0), model.apply(context, stackCall(RuntimeNames.STACK_IS_EMPTY, stack(Items.EMERALD, 1))));
        assertEquals(new Value.Known(1), model.apply(context, stackCall(RuntimeNames.STACK_IS_EMPTY, stack(Items.EMERALD, 0))));
        assertEquals("stack emptiness", assertThrows(Fail.class, () -> model.apply(context, stackCall(RuntimeNames.STACK_IS_EMPTY, random))).getMessage());
    }

    @Test
    public void testOtherCallRunsOnEachTemplate() {
        Value.Stack stack = new Value.Stack(List.of(new ItemStack(Items.EMERALD), new ItemStack(Items.DIAMOND)), NumberExpr.constant(2), false);

        context.concreteCall = (call) -> null;
        assertNull(model.apply(context, call(INVOKEVIRTUAL, ITEM_STACK, "enchant", ENCHANT, stack, new Value.Known(null), new Value.Known(1))));
        assertEquals(List.of(stack.options.get(0), stack.options.get(1)), context.concreteCalls.stream().map((c) -> ((Value.Known) c.args().get(0)).value()).toList());
        assertEquals(List.of(2, 2), stack.options.stream().map(ItemStack::getCount).toList());
    }

    @Test
    public void testCallReturningTemplateReturnsStack() {
        Value.Stack stack = stack(Items.EMERALD, 1);

        context.concreteCall = (call) -> call.args().get(0);
        assertSame(stack, model.apply(context, call(INVOKEVIRTUAL, ITEM_STACK, "setHoverName", HOVER_NAME, stack, new Value.Known(null))));
    }

    @Test
    public void testCallReturningPartOfStackRecordsIt() {
        Value.Stack stack = stack(Items.EMERALD, 1);
        CompoundTag tag = new CompoundTag();

        context.concreteCall = (call) -> new Value.Known(tag);
        assertEquals(new Value.Known(tag), model.apply(context, call(INVOKEVIRTUAL, ITEM_STACK, "getOrCreateTag", "()Lnet/minecraft/nbt/CompoundTag;", stack)));
        assertSame(stack, context.stackParts.get(tag));
        assertTrue(context.owned.contains(tag));
    }

    @Test
    public void testCallWithSymbolicArgumentMarksRandomProperties() {
        Value.Stack stack = stack(Items.EMERALD, 1);

        assertNull(model.apply(context, call(INVOKEVIRTUAL, ITEM_STACK, "enchant", ENCHANT, stack, new Value.Known(null), ONE_TO_THREE)));
        assertTrue(stack.randomTag);
        assertTrue(context.concreteCalls.isEmpty());
    }

    @Test
    public void testCallWithUncertainArgumentReturnsMarkedCopy() {
        Value.Stack stack = stack(Items.EMERALD, 1);
        Object name = new Object();

        context.tainted.add(name);

        Value.Stack copy = assertInstanceOf(Value.Stack.class, model.apply(context, call(INVOKEVIRTUAL, ITEM_STACK, "setHoverName", HOVER_NAME, stack, new Value.Known(name))));

        assertNotSame(stack, copy);
        assertTrue(stack.randomTag);
        assertTrue(copy.randomTag);
    }

    @Test
    public void testCopyOfRealStackIsModelledStack() {
        Value.Stack copy = assertInstanceOf(Value.Stack.class, model.apply(context, stackCall(RuntimeNames.STACK_COPY, new Value.Known(new ItemStack(Items.EMERALD, 2)))));

        assertTrue(copy.options.get(0).is(Items.EMERALD));
        assertEquals(NumberExpr.constant(2), copy.count);
    }

    @Test
    public void testOtherCallOnRealStackRunsOnShadow() {
        ItemStack real = new ItemStack(Items.EMERALD);

        context.concreteCall = (call) -> null;
        assertNull(model.apply(context, call(INVOKEVIRTUAL, ITEM_STACK, "enchant", ENCHANT, new Value.Known(real), new Value.Known(null), new Value.Known(1))));
        assertSame(context.shadows.get(real).options.get(0), ((Value.Known) context.concreteCalls.get(0).args().get(0)).value());
    }

    @Test
    public void testFixedStackArgumentsAreRealStacks() {
        Value.Stack cost = stack(Items.EMERALD, 3);
        Value.Stack result = stack(Items.BREAD, 1);
        Object offer = new Object();

        result.randomTag = true;
        context.concreteCall = (call) -> new Value.Known(offer);
        assertEquals(new Value.Known(offer), ItemStackModel.argumentCall(context, call(INVOKESTATIC, "mod/Trades", "trade", TRADE, cost, result)));

        List<Value> args = context.concreteCalls.get(0).args();
        ItemStack realCost = (ItemStack) ((Value.Known) args.get(0)).value();

        assertTrue(realCost.is(Items.EMERALD));
        assertEquals(3, realCost.getCount());
        assertTrue(context.owned.contains(realCost));
        assertSame(result.options.get(0), ((Value.Known) args.get(1)).value());
    }

    @Test
    public void testChangedFixedStackArgumentFails() {
        context.concreteCall = (call) -> {
            ((ItemStack) ((Value.Known) call.args().get(1)).value()).setCount(5);
            return new Value.Known(new Object());
        };

        Call call = call(INVOKESTATIC, "mod/Trades", "trade", TRADE, stack(Items.EMERALD, 3), stack(Items.BREAD, 1));

        assertEquals("stack argument changed", assertThrows(Fail.class, () -> ItemStackModel.argumentCall(context, call)).getMessage());
    }

    @Test
    public void testSeveralChangingStackArgumentsFail() {
        Value.Stack cost = stack(Items.EMERALD, 3);
        Value.Stack result = stack(Items.BREAD, 1);

        cost.randomTag = true;
        result.randomTag = true;

        Call call = call(INVOKESTATIC, "mod/Trades", "trade", TRADE, cost, result);

        assertEquals("several stack arguments", assertThrows(Fail.class, () -> ItemStackModel.argumentCall(context, call)).getMessage());
    }

    @Test
    public void testOtherReceiverHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", new Value.Known("a"))));
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKESTATIC, "mod/Util", "copy", "(Lnet/minecraft/world/item/ItemStack;)V", stack(Items.EMERALD, 1))));
    }

    private static Value.Stack stack(net.minecraft.world.item.Item item, int count) {
        return new Value.Stack(List.of(new ItemStack(item)), NumberExpr.constant(count), false);
    }

    private static Call stackCall(RuntimeNames.Ref ref, Value... args) {
        return call(INVOKEVIRTUAL, ITEM_STACK, ref.name(), ref.desc(), args);
    }

    private static Call construct(String type, String desc, Value... args) {
        return call(INVOKESPECIAL, type, Interpreter.CONSTRUCTOR, desc, args);
    }
}
