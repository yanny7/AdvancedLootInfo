package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class MerchantOfferModelTest {
    private static final String MERCHANT_OFFER = "net/minecraft/world/item/trading/MerchantOffer";
    private static final String COST_AND_RESULT = "(Lnet/minecraft/world/item/trading/ItemCost;Lnet/minecraft/world/item/ItemStack;IIF)V";
    private static final String TWO_COSTS = "(Lnet/minecraft/world/item/trading/ItemCost;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;IIIF)V";

    private final MerchantOfferModel model = new MerchantOfferModel();
    private final FakeCallContext context = new FakeCallContext();
    private final Value.Stack cost = new Value.Stack(List.of(new ItemStack(Items.EMERALD)), NumberExpr.constant(3), false);
    private final Value.Stack result = new Value.Stack(List.of(new ItemStack(Items.BREAD)), NumberExpr.constant(1), false);

    @Test
    public void testCostAndResult() {
        Value offer = model.apply(context, construct(MERCHANT_OFFER, COST_AND_RESULT, cost, result, new Value.Known(12), new Value.Known(5), new Value.Known(0.05F)));

        assertEquals(new Value.Offer(cost, new Value.Known(ItemStack.EMPTY), result, 12, 5, 0.05F), offer);
    }

    @Test
    public void testUsesBeforeMaxUsesAreSkipped() {
        Value offer = model.apply(context, construct(MERCHANT_OFFER, TWO_COSTS, cost, cost, result, new Value.Known(0), new Value.Known(12), new Value.Known(5),
                new Value.Known(0.05F)));

        assertEquals(new Value.Offer(cost, cost, result, 12, 5, 0.05F), offer);
    }

    @Test
    public void testConcreteCostsAreTheirStacks() {
        ItemCost itemCost = new ItemCost(Items.EMERALD, 3);
        Value offer = model.apply(context, construct(MERCHANT_OFFER, TWO_COSTS, new Value.Known(itemCost), new Value.Known(Optional.empty()), result, new Value.Known(0),
                new Value.Known(12), new Value.Known(5), new Value.Known(0.05F)));

        assertEquals(new Value.Offer(new Value.Known(itemCost.itemStack()), new Value.Known(ItemStack.EMPTY), result, 12, 5, 0.05F), offer);
    }

    @Test
    public void testSymbolicUsesFail() {
        Call call = construct(MERCHANT_OFFER, COST_AND_RESULT, cost, result, new Value.Num(NumberExpr.uniformInt(1, 3), Value.Kind.INT), new Value.Known(5),
                new Value.Known(0.05F));

        assertEquals("symbolic trade number", assertThrows(Fail.class, () -> model.apply(context, call)).getMessage());
    }

    @Test
    public void testForwardingSubclassIsOffer() {
        Value offer = model.apply(context, construct(Type.getInternalName(ForwardingOffer.class), COST_AND_RESULT, cost, result, new Value.Known(12), new Value.Known(5),
                new Value.Known(0.05F)));

        assertEquals(new Value.Offer(cost, new Value.Known(ItemStack.EMPTY), result, 12, 5, 0.05F), offer);
    }

    @Test
    public void testSubclassChangingArgumentsHasNoOpinion() {
        Call call = construct(Type.getInternalName(FixedUsesOffer.class), "(Lnet/minecraft/world/item/trading/ItemCost;Lnet/minecraft/world/item/ItemStack;IF)V", cost, result,
                new Value.Known(5), new Value.Known(0.05F));

        assertSame(MethodModel.NO_OPINION, model.apply(context, call));
    }

    @Test
    public void testOtherConstructorHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, construct("java/util/ArrayList", "(Ljava/util/Collection;)V", cost)));
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKESPECIAL, MERCHANT_OFFER, "getResult", "()Lnet/minecraft/world/item/ItemStack;", cost)));
    }

    private static Call construct(String type, String desc, Value... args) {
        return call(INVOKESPECIAL, type, Interpreter.CONSTRUCTOR, desc, args);
    }

    static class ForwardingOffer extends MerchantOffer {
        ForwardingOffer(ItemCost cost, ItemStack result, int maxUses, int xp, float priceMultiplier) {
            super(cost, result, maxUses, xp, priceMultiplier);
        }
    }

    static class FixedUsesOffer extends MerchantOffer {
        FixedUsesOffer(ItemCost cost, ItemStack result, int xp, float priceMultiplier) {
            super(cost, result, 12, xp, priceMultiplier);
        }
    }
}
