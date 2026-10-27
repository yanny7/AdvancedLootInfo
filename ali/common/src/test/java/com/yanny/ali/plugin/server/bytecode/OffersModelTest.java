package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.VillagerTrade;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.objectweb.asm.Opcodes.*;

public class OffersModelTest {
    private static final String OFFERS = Type.getInternalName(MerchantOffers.class);
    private static final String ADD = "(Ljava/lang/Object;)Z";
    private static final String ADD_ALL = "(Ljava/util/Collection;)Z";
    private static final String REMOVE_IF = "(Ljava/util/function/Predicate;)Z";
    private static final VillagerTrade FIRST = TestTrades.of(Items.APPLE);
    private static final VillagerTrade SECOND = TestTrades.of(Items.BREAD);

    private final OffersModel model = new OffersModel();
    private final FakeCallContext context = new FakeCallContext();
    private final Value.Offers offers = new Value.Offers();

    @Test
    public void testNewPlainOffersAreSymbolic() {
        assertInstanceOf(Value.Offers.class, OffersModel.NEW.apply(context, call(INVOKESPECIAL, OFFERS, Interpreter.CONSTRUCTOR, "()V")));
    }

    @Test
    public void testNewOffersOverridingAddHaveNoOpinion() {
        assertSame(MethodModel.NO_OPINION, OffersModel.NEW.apply(context, call(INVOKESPECIAL, Type.getInternalName(OwnOffers.class), Interpreter.CONSTRUCTOR, "()V")));
    }

    @Test
    public void testAddedPicksOfOnePoolAreOneGroup() {
        Object pool = new Object();

        assertEquals(new Value.Known(1), offersCall("add", ADD, new Value.Pick(List.of(FIRST), pool, 2)));
        assertEquals(new Value.Known(1), offersCall("add", ADD, new Value.Pick(List.of(SECOND), pool, 2)));
        assertEquals(1, offers.entries.size());

        Value.Offers.Listings listings = assertInstanceOf(Value.Offers.Listings.class, offers.entries.get(0));

        assertArrayEquals(new Object[]{FIRST, SECOND}, listings.listings());
        assertEquals(new Value.Known(2), listings.count());
        assertEquals(2, listings.key());
    }

    @Test
    public void testAddedPickOfDrawnCountCountsTheDraw() {
        Object pool = new Object();
        NumberExpr count = NumberExpr.uniformInt(1, 3);

        context.drawCounts.put(pool, count);
        offersCall("add", ADD, new Value.Pick(List.of(FIRST, SECOND), pool, null));
        assertEquals(new Value.Num(count, Value.Kind.INT), ((Value.Offers.Listings) offers.entries.get(0)).count());
    }

    @Test
    public void testAddedOfferIsSingleTrade() {
        Value offer = new Value.Known("offer");

        offersCall("add", ADD, offer);
        assertEquals(List.of(new Value.Offers.Single(offer)), offers.entries);
    }

    @Test
    public void testAddAllOfOffersTakesTheirTradesAndGroups() {
        Value.Offers source = new Value.Offers();
        Object pool = new Object();

        offers.entries.add(new Value.Offers.Single(new Value.Known("first")));
        source.entries.add(new Value.Offers.Listings(new Object[]{FIRST}, new Value.Known(1), null));
        source.picks.put(pool, 0);
        assertEquals(new Value.Known(1), offersCall("addAll", ADD_ALL, source));
        assertEquals(2, offers.entries.size());
        assertEquals(1, offers.picks.get(pool));
    }

    @Test
    public void testAddAllOfKnownCollectionAddsEachElement() {
        offersCall("addAll", ADD_ALL, new Value.Known(List.of("first", "second")));
        assertEquals(List.of(new Value.Offers.Single(new Value.Known("first")), new Value.Offers.Single(new Value.Known("second"))), offers.entries);
    }

    @Test
    public void testRemoveIfDropsTradesThePredicateHolds() {
        offers.entries.add(new Value.Offers.Single(new Value.Known("first")));
        context.invokeResult = new Value.Known(1);
        assertEquals(new Value.Known(1), offersCall("removeIf", REMOVE_IF, predicate("mod/Filters", "remove")));
        assertTrue(offers.entries.isEmpty());
    }

    @Test
    public void testRemoveIfIsNullKeepsPicks() {
        offers.entries.add(new Value.Offers.Listings(new Object[]{FIRST}, new Value.Known(1), null));
        assertEquals(new Value.Known(0), offersCall("removeIf", REMOVE_IF, predicate("java/util/Objects", "isNull")));
        assertEquals(1, offers.entries.size());
    }

    @Test
    public void testRemoveIfOfOtherPredicateOnPicksFails() {
        offers.entries.add(new Value.Offers.Listings(new Object[]{FIRST}, new Value.Known(1), null));
        assertEquals("merchant offers call removeIf", assertThrows(Fail.class, () -> offersCall("removeIf", REMOVE_IF, predicate("mod/Filters", "remove"))).getMessage());
    }

    @Test
    public void testClearEmptiesOffers() {
        offers.entries.add(new Value.Offers.Single(new Value.Known("first")));
        offers.picks.put(new Object(), 0);
        assertNull(offersCall("clear", "()V"));
        assertTrue(offers.entries.isEmpty());
        assertTrue(offers.picks.isEmpty());
    }

    @Test
    public void testSortAndIsEmptyAreAnswered() {
        assertNull(offersCall("sort", "(Ljava/util/Comparator;)V", Interpreter.NULL));
        assertEquals(new Value.Known(1), offersCall("isEmpty", "()Z"));
    }

    @Test
    public void testOtherOffersCallFails() {
        assertEquals("merchant offers call iterator", assertThrows(Fail.class, () -> offersCall("iterator", "()Ljava/util/Iterator;")).getMessage());
    }

    @Test
    public void testAddOfPickToOwnedListMakesItOffers() {
        List<Object> list = new ArrayList<>();

        context.owned.add(list);
        assertEquals(new Value.Known(1), OffersModel.COLLECTED.apply(context, call(INVOKEINTERFACE, "java/util/List", "add", ADD, new Value.Known(list),
                new Value.Pick(List.of(FIRST), new Object(), null))));
        assertEquals(1, context.offers(list).entries.size());
    }

    @Test
    public void testAddOfDrawnOffersToOwnedListIsPickedGroup() {
        List<Object> list = new ArrayList<>();
        Object pool = new Object();
        MerchantOffer first = mock(MerchantOffer.class);
        MerchantOffer second = mock(MerchantOffer.class);

        context.owned.add(list);
        context.pools.add(pool);
        OffersModel.COLLECTED.apply(context, call(INVOKEINTERFACE, "java/util/List", "add", ADD, new Value.Known(list), new Value.Choice(List.of(first, second), pool)));
        assertEquals(List.of(new Value.Offers.Picked(List.of(first, second), new Value.Known(1))), context.offers(list).entries);
    }

    @Test
    public void testCallOnCollectedOffersActsOnThem() {
        List<Object> list = new ArrayList<>();

        context.coll(list).offers = offers;
        assertEquals(new Value.Known(1), OffersModel.COLLECTED.apply(context, call(INVOKEINTERFACE, "java/util/List", "isEmpty", "()Z", new Value.Known(list))));
    }

    @Test
    public void testAddOfOtherValueToOwnedListHasNoOpinion() {
        List<Object> list = new ArrayList<>();

        context.owned.add(list);
        assertSame(MethodModel.NO_OPINION, OffersModel.COLLECTED.apply(context, call(INVOKEINTERFACE, "java/util/List", "add", ADD, new Value.Known(list), new Value.Known("x"))));
    }

    @Test
    public void testAddOfPickToFrozenListFails() {
        List<Object> list = new ArrayList<>();

        context.owned.add(list);
        context.frozen.add(list);
        assertEquals("offers in a collection", assertThrows(Fail.class, () -> OffersModel.COLLECTED.apply(context, call(INVOKEINTERFACE, "java/util/List", "add", ADD,
                new Value.Known(list), new Value.Pick(List.of(FIRST), new Object(), null)))).getMessage());
    }

    @Test
    public void testShuffleOfOffersChangesNothing() {
        assertNull(OffersModel.ORDER.apply(context, call(INVOKESTATIC, "java/util/Collections", "shuffle", "(Ljava/util/List;)V", offers)));
        assertSame(MethodModel.NO_OPINION, OffersModel.ORDER.apply(context, call(INVOKESTATIC, "java/util/Collections", "shuffle", "(Ljava/util/List;)V", Interpreter.NULL)));
    }

    private Value offersCall(String name, String desc, Value... args) {
        Value[] all = new Value[args.length + 1];

        all[0] = offers;
        System.arraycopy(args, 0, all, 1, args.length);
        return model.apply(context, call(INVOKEVIRTUAL, OFFERS, name, desc, all));
    }

    private static Value.Closure predicate(String owner, String name) {
        Handle impl = new Handle(H_INVOKESTATIC, owner, name, "(Ljava/lang/Object;)Z", false);

        return new Value.Closure(Predicate.class, "test", "(Ljava/lang/Object;)Z", impl, List.of(), OffersModelTest.class.getClassLoader());
    }

    public static class OwnOffers extends MerchantOffers {
        @Override
        public boolean add(MerchantOffer offer) {
            return false;
        }
    }
}
