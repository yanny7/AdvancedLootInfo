package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.npc.VillagerTrades;
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
    public void testAddOffersFromItemListingsAddsGroupWithLevel() {
        VillagerTrades.ItemListing[] listings = {(trader, random) -> null};
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

    private static Call addOffers(Value.Offers offers, Value listings, Value count) {
        RuntimeNames.Ref ref = RuntimeNames.ADD_OFFERS_FROM_ITEM_LISTINGS;

        return call(INVOKEVIRTUAL, TRADER, ref.name(), ref.desc(), SELF, offers, listings, count);
    }

    public static class Trader {
    }
}
