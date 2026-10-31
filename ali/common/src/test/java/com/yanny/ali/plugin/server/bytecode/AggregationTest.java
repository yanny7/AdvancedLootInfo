package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.VillagerTrade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationTest {
    private static final VillagerTrade FIRST = TestTrades.of(Items.APPLE);
    private static final VillagerTrade SECOND = TestTrades.of(Items.BREAD);
    private static final Decision UNKNOWN = new Decision(Decision.Kind.UNKNOWN, null, null, true);
    private static final Decision LEVEL_2 = new Decision(Decision.Kind.EQUALS, "level", 2, true);

    @Test
    public void testListingSameOfferOnEveryPathIsAlways() {
        Aggregation.Listing listing = Aggregation.listing(List.of(success(offer(Items.APPLE), UNKNOWN), success(offer(Items.APPLE), UNKNOWN.negated())));

        assertNull(listing.reason());
        assertEquals(1, listing.offers().size());
        assertSame(PathCondition.ALWAYS, listing.offers().getFirst().when());
    }

    @Test
    public void testListingOffersOfSomePathsGetTheirCondition() {
        Aggregation.Listing listing = Aggregation.listing(List.of(success(offer(Items.APPLE), random(0.5, true)), success(offer(Items.BREAD), random(0.5, false))));

        assertEquals(2, listing.offers().size());
        assertInstanceOf(PathCondition.RandomChance.class, listing.offers().getFirst().when());
        assertInstanceOf(PathCondition.RandomChance.class, listing.offers().get(1).when());
    }

    @Test
    public void testListingKeepsFirstFailureAndOtherOffers() {
        Aggregation.Listing listing = Aggregation.listing(List.of(success(offer(Items.APPLE), UNKNOWN), failure("first", UNKNOWN.negated()), failure("second")));

        assertEquals("first", listing.reason());
        assertEquals(1, listing.offers().size());
    }

    @Test
    public void testListingUnrenderableResultIsReason() {
        Aggregation.Listing listing = Aggregation.listing(List.of(success(offer(Items.APPLE), UNKNOWN), success(new Value.Known("text"), UNKNOWN.negated())));

        assertEquals("offer that cannot be shown", listing.reason());
        assertEquals(1, listing.offers().size());
    }

    @Test
    public void testListingNoOfferDropsOut() {
        Aggregation.Listing listing = Aggregation.listing(List.of(success(offer(Items.APPLE), UNKNOWN), success(Interpreter.NULL, UNKNOWN.negated())));

        assertNull(listing.reason());
        assertEquals(1, listing.offers().size());
    }

    @Test
    public void testListingWithoutOfferNamesLastResult() {
        Aggregation.Listing listing = Aggregation.listing(List.of(success(Interpreter.NULL, UNKNOWN), success(null, UNKNOWN.negated())));

        assertEquals("no offer returned (null)", listing.reason());
        assertTrue(listing.offers().isEmpty());
    }

    @Test
    public void testListingRandomPropertiesAbsorbSameItem() {
        Value.Stack random = new Value.Stack(List.of(new ItemStack(Items.APPLE)), NumberExpr.constant(1), true);
        Value.Offer randomOffer = new Value.Offer(new Value.Known(new ItemStack(Items.EMERALD)), new Value.Known(ItemStack.EMPTY), random, 12, 1, 0.05F);
        Aggregation.Listing listing = Aggregation.listing(List.of(success(randomOffer, UNKNOWN), success(offer(Items.APPLE), UNKNOWN.negated())));

        assertEquals(1, listing.offers().size());
        assertSame(randomOffer, listing.offers().getFirst().result());
        assertSame(PathCondition.ALWAYS, listing.offers().getFirst().when());
    }

    @Test
    public void testTraderListingsWithoutDecisionsAreLevelOne() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(listings(2, FIRST, SECOND)))));

        assertNull(trader.reason());
        assertEquals(Set.of(1), trader.levels().keySet());

        Aggregation.Group group = trader.levels().get(1).groups().getFirst();

        assertEquals(1F, trader.levels().get(1).chance());
        assertSame(PathCondition.ALWAYS, group.condition());
        assertEquals(NumberExpr.constant(2), group.count());
    }

    @Test
    public void testTraderLevelFromIntegerEquality() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(listings(1, FIRST)), LEVEL_2), success(offers(listings(1, SECOND)), LEVEL_2.negated())));

        assertNull(trader.reason());
        assertEquals(Set.of(1, 2), trader.levels().keySet());
    }

    @Test
    public void testTraderFailureNamingLevelMarksIt() {
        Aggregation.Trader trader = Aggregation.trader(List.of(failure("broken", LEVEL_2), success(offers(listings(1, FIRST)), LEVEL_2.negated())));

        assertEquals("broken", trader.reason());
        assertFalse(trader.unknownLevel());
        assertEquals(Set.of(2), trader.incomplete());
        assertEquals(Set.of(1), trader.levels().keySet());
    }

    @Test
    public void testTraderFailureWithoutLevelIsUnknownLevel() {
        Aggregation.Trader trader = Aggregation.trader(List.of(failure("broken", UNKNOWN), success(offers(listings(1, FIRST)), UNKNOWN.negated())));

        assertTrue(trader.unknownLevel());
        assertTrue(trader.incomplete().isEmpty());
    }

    @Test
    public void testTraderRandomCountsAreWeighted() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(listings(1, FIRST)), random(0.25, true)), success(offers(listings(2, FIRST)), random(0.25, false))));
        Aggregation.Group group = trader.levels().get(1).groups().getFirst();

        assertNull(trader.reason());
        assertEquals(1F, trader.levels().get(1).chance());
        assertEquals(NumberExpr.weighted(List.of(new NumberExpr.WeightedEntry(0.25, NumberExpr.constant(1)), new NumberExpr.WeightedEntry(0.75, NumberExpr.constant(2)))),
                group.count());
    }

    @Test
    public void testTraderGroupsOfOneLevelShareTheirChance() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(listings(1, FIRST)), random(0.5, true)), success(offers(listings(1, SECOND)), random(0.5, false))));

        assertNull(trader.reason());
        assertEquals(2, trader.levels().get(1).groups().size());
        assertEquals(0.5F, trader.levels().get(1).chance());
    }

    @Test
    public void testTraderGroupsOfDifferentChancesAreIncomplete() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(listings(1, FIRST)), random(0.25, true)), success(offers(listings(1, SECOND)), random(0.25, false))));

        assertEquals("several sets of trades of one level", trader.reason());
        assertEquals(Set.of(1), trader.incomplete());
        assertTrue(trader.levels().isEmpty());
    }

    @Test
    public void testTraderSameListingsTwiceInOnePath() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(listings(1, FIRST), listings(1, FIRST)))));

        assertEquals("the same trades added twice", trader.reason());
        assertEquals(Set.of(1), trader.incomplete());
    }

    @Test
    public void testTraderSameSingleTwiceIsRepeated() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(single(offer(Items.APPLE)), single(offer(Items.APPLE))))));
        Aggregation.Group group = trader.levels().get(1).groups().getFirst();

        assertNull(trader.reason());
        assertEquals(2, group.times());
        assertEquals(NumberExpr.constant(2), group.count());
    }

    @Test
    public void testTraderUnrenderableSingleIsLeftOut() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(single(new Value.Known("text")), listings(1, FIRST)))));

        assertEquals("offer that cannot be shown", trader.reason());
        assertEquals(Set.of(1), trader.incomplete());
        assertEquals(1, trader.levels().get(1).groups().size());
    }

    @Test
    public void testTraderWithoutTradesAfterDecisions() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers(), UNKNOWN), success(offers(), UNKNOWN.negated())));

        assertEquals("no trades", trader.reason());
        assertTrue(trader.unknownLevel());
    }

    @Test
    public void testTraderWithoutTradesOrDecisionsIsComplete() {
        Aggregation.Trader trader = Aggregation.trader(List.of(success(offers())));

        assertNull(trader.reason());
        assertTrue(trader.levels().isEmpty());
    }

    @Test
    public void testTraderUnknownCountFailsKeepingMarkedLevels() {
        Aggregation.Trader trader = Aggregation.trader(List.of(failure("broken", LEVEL_2), success(offers(new Value.Offers.Listings(new Object[]{FIRST}, Value.UNKNOWN, null)), LEVEL_2.negated())));

        assertEquals("number of offers", trader.reason());
        assertTrue(trader.unknownLevel());
        assertEquals(Set.of(2), trader.incomplete());
        assertTrue(trader.levels().isEmpty());
    }

    @Test
    public void testStacksOfChoiceMergeSameStacks() {
        BytecodeInterpreter.Stacks stacks = Aggregation.stacks(new Value.Choice(List.of(new ItemStack(Items.APPLE), new ItemStack(Items.APPLE), new ItemStack(Items.BREAD), ItemStack.EMPTY), null));

        assertNotNull(stacks);
        assertEquals(List.of(Items.APPLE, Items.BREAD), stacks.options().stream().map((o) -> o.stack().getItem()).toList());
        assertEquals(List.of(0.5, 0.25), stacks.options().stream().map(BytecodeInterpreter.Option::chance).toList());
    }

    @Test
    public void testStacksOfModelledStackKeepItsCount() {
        NumberExpr count = NumberExpr.range(1, 3);
        BytecodeInterpreter.Stacks stacks = Aggregation.stacks(new Value.Stack(List.of(new ItemStack(Items.APPLE)), count, false));

        assertNotNull(stacks);
        assertEquals(1, stacks.options().size());
        assertEquals(count, stacks.options().getFirst().count());
        assertEquals(1, stacks.options().getFirst().stack().getCount());
    }

    @Test
    public void testStacksOfOtherValueAreNull() {
        assertNull(Aggregation.stacks(new Value.Known("text")));
        assertNull(Aggregation.stacks(null));
    }

    private static Outcome success(Value result, Decision... decisions) {
        return new Outcome.Success(result, List.of(decisions), null);
    }

    private static Outcome failure(String reason, Decision... decisions) {
        return new Outcome.Failure(reason, List.of(decisions), null);
    }

    private static Decision random(double chance, boolean holds) {
        return new Decision(Decision.Kind.RANDOM, 0, chance, holds);
    }

    private static Value.Offer offer(Item result) {
        return new Value.Offer(new Value.Known(new ItemStack(Items.EMERALD)), new Value.Known(ItemStack.EMPTY), new Value.Known(new ItemStack(result)), 12, 1, 0.05F);
    }

    private static Value.Offers offers(Value.Offers.Entry... entries) {
        Value.Offers offers = new Value.Offers();

        offers.entries.addAll(List.of(entries));
        return offers;
    }

    private static Value.Offers.Entry listings(int count, Object... listings) {
        return new Value.Offers.Listings(listings, new Value.Known(count), null);
    }

    private static Value.Offers.Entry single(Value offer) {
        return new Value.Offers.Single(offer);
    }
}
