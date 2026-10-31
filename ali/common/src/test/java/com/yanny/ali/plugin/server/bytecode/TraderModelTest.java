package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class TraderModelTest {
    private static final String TRADER = Type.getInternalName(Trader.class);
    private static final Value.Unknown SELF = Value.Unknown.of(Type.getType(Trader.class), Origin.SELF);

    private final TraderModel model = new TraderModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testGetOffersIsSymbolicOffers() {
        TraderSubject run = run(false);

        assertSame(run.offers, model.apply(context, call(INVOKEVIRTUAL, TRADER, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), SELF)));
    }

    @Test
    public void testOwnGetOffersIsNotSymbolic() {
        run(true);

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), SELF)));
    }

    @Test
    public void testAddOffersFromTradeSetAddsTheSetOnce() {
        ResourceKey<TradeSet> tradeSet = TradeSets.WANDERING_TRADER_BUYING;
        Value.Offers offers = new Value.Offers();

        run(false);
        assertNull(model.apply(context, addOffers(offers, new Value.Known(tradeSet))));
        assertEquals(1, offers.entries.size());

        Value.Offers.Listings listings = assertInstanceOf(Value.Offers.Listings.class, offers.entries.get(0));

        assertArrayEquals(new Object[]{tradeSet}, listings.listings());
        assertEquals(new Value.Known(1), listings.count());
        assertNull(listings.key());
    }

    @Test
    public void testAddOffersFromUnknownTradeSetFails() {
        run(false);

        assertEquals("offers from trade set", assertThrows(Fail.class, () -> model.apply(context, addOffers(new Value.Offers(), Value.UNKNOWN))).getMessage());
    }

    @Test
    public void testOtherReceiverHasNoOpinion() {
        run(false);

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), Value.UNKNOWN)));
    }

    @Test
    public void testNoSubjectHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, TRADER, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), SELF)));
    }

    private TraderSubject run(boolean ownGetOffers) {
        TraderSubject run = new TraderSubject(Trader.class, ownGetOffers);

        context.subject = run;
        return run;
    }

    private static Call addOffers(Value.Offers offers, Value tradeSet) {
        RuntimeNames.Ref ref = RuntimeNames.ADD_OFFERS_FROM_TRADE_SET;

        return call(INVOKEVIRTUAL, TRADER, ref.name(), ref.desc(), SELF, Value.Unknown.of(Type.getArgumentTypes(ref.desc())[0], Origin.WORLD), offers, tradeSet);
    }

    public static class Trader {
    }
}
