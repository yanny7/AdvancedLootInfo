package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.npc.villager.VillagerTrades;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class TraderModelTest {
    private static final String TRADER = Type.getInternalName(Trader.class);
    private static final Value.Unknown SELF = Value.Unknown.of(Type.getType(Trader.class), Origin.TRADER);

    private final TraderModel model = new TraderModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testGetOffersIsSymbolicOffers() {
        TraderModel.TraderRun run = run(false);

        assertSame(run.offers, model.apply(context, call(INVOKEVIRTUAL, TRADER, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), SELF)));
    }

    @Test
    public void testOwnGetOffersIsNotSymbolic() {
        run(true);

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), SELF)));
    }

    @Test
    public void testAddOffersFromItemListingsAddsGroupWithLevel() {
        VillagerTrades.ItemListing[] listings = {(world, trader, random) -> null};
        Value.Offers offers = new Value.Offers();
        Value count = new Value.Known(2);

        run(false);
        context.levelKeys.put(listings, 3);
        assertNull(model.apply(context, addOffers(offers, new Value.Known(listings), count)));
        assertEquals(List.of(new Value.Offers.Listings(listings, count, 3)), offers.entries);
    }

    @Test
    public void testAddOffersFromUnknownListingsFails() {
        run(false);

        assertEquals("offers from item listings", assertThrows(Fail.class, () -> model.apply(context, addOffers(new Value.Offers(), Value.UNKNOWN, new Value.Known(2)))).getMessage());
    }

    @Test
    public void testOwnMethodIsInterpreted() {
        run(false);

        assertNull(model.apply(context, call(INVOKEVIRTUAL, TRADER, "level", "()I", SELF)));
        assertEquals(1, context.executions.size());
        assertEquals(Trader.class, context.executions.getFirst().owner());
        assertEquals("level", context.executions.getFirst().method().name);
        assertEquals(List.of(SELF), context.executions.getFirst().args());
    }

    @Test
    public void testSpecialCallStartsAtCalledOwner() {
        run(false);

        model.apply(context, call(INVOKESPECIAL, Type.getInternalName(BaseTrader.class), "level", "()I", SELF));
        assertEquals(BaseTrader.class, context.executions.getFirst().owner());
    }

    @Test
    public void testJdkMethodHasNoOpinion() {
        run(false);

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, "hashCode", "()I", SELF)));
        assertTrue(context.executions.isEmpty());
    }

    @Test
    public void testOtherReceiverHasNoOpinion() {
        run(false);

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, "level", "()I", Value.UNKNOWN)));
    }

    @Test
    public void testNoTraderRunHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, "level", "()I", SELF)));
    }

    private TraderModel.TraderRun run(boolean ownGetOffers) {
        TraderModel.TraderRun run = new TraderModel.TraderRun(Trader.class, ownGetOffers);

        context.traderRun = run;
        return run;
    }

    private static Call addOffers(Value.Offers offers, Value listings, Value count) {
        RuntimeNames.Ref ref = RuntimeNames.ADD_OFFERS_FROM_ITEM_LISTINGS;

        return call(INVOKEVIRTUAL, TRADER, ref.name(), ref.desc(), SELF, Value.Unknown.of(Type.getArgumentTypes(ref.desc())[0], Origin.WORLD), offers, listings, count);
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
