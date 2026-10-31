package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.VillagerTrade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class PickModelTest {
    private static final String LISTING = Type.getInternalName(VillagerTrade.class);
    private static final String CREATE = "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/trading/MerchantOffer;";
    private static final String NAME = "(Lnet/minecraft/world/entity/Entity;)Ljava/lang/String;";
    private static final Value.Unknown SELF = Value.Unknown.of(Type.getType(Object.class), Origin.SELF);
    private static final VillagerTrade FIRST = TestTrades.of(Items.APPLE);
    private static final VillagerTrade SECOND = TestTrades.of(Items.BREAD);

    private final PickModel model = new PickModel();
    private final FakeCallContext context = new FakeCallContext();

    @BeforeEach
    public void setUp() {
        context.subject = new TraderSubject(Object.class, false);
    }

    @Test
    public void testGetOfferOnPoolIsPickOfItsListings() {
        Object pool = new Object();
        Value.Choice choice = new Value.Choice(List.of(FIRST, SECOND), pool);

        context.pools.add(pool);
        context.poolKeys.put(List.of(FIRST, SECOND), 2);
        assertEquals(new Value.Pick(List.of(FIRST, SECOND), pool, 2), model.apply(context, getOffer(choice)));
    }

    @Test
    public void testGetOfferOnUniformDrawIsPick() {
        NumberExpr draw = NumberExpr.uniformInt(0, 1);

        assertEquals(new Value.Pick(List.of(FIRST, SECOND), draw, null), model.apply(context, getOffer(new Value.Choice(List.of(FIRST, SECOND), draw))));
    }

    @Test
    public void testGetOfferOnOtherChoiceHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, getOffer(new Value.Choice(List.of(FIRST, SECOND), new Object()))));
    }

    @Test
    public void testGetOfferOnKnownListingIsPickFromItsSource() {
        Object source = new VillagerTrade[]{FIRST, SECOND};

        context.listingSources.put(FIRST, source);
        context.levelKeys.put(source, 3);
        assertEquals(new Value.Pick(List.of(FIRST), source, 3), model.apply(context, getOffer(new Value.Known(FIRST))));
    }

    @Test
    public void testGetOfferOnKnownListingWithoutSourceIsItsOwnPool() {
        assertEquals(new Value.Pick(List.of(FIRST), FIRST, null), model.apply(context, getOffer(new Value.Known(FIRST))));
    }

    @Test
    public void testOfferMethodOnDrawnElementsIsPickOfBoundOffers() {
        Object first = new Object();
        Object second = new Object();
        NumberExpr draw = NumberExpr.uniformInt(0, 1);
        Value.Pick pick = assertInstanceOf(Value.Pick.class, model.apply(context, call(INVOKEVIRTUAL, "mod/Trade", "create", CREATE,
                new Value.Choice(List.of(first, second), draw), SELF, new Value.Rng(false))));
        List<Object> bound = List.of(BoundOffer.Bound.TRADER, BoundOffer.Bound.RANDOM);

        assertEquals(List.of(new BoundOffer(first, "mod/Trade", "create", CREATE, false, bound), new BoundOffer(second, "mod/Trade", "create", CREATE, false, bound)),
                pick.listings());
        assertSame(draw, pick.pool());
    }

    @Test
    public void testOfferMethodWithSymbolicArgumentHasNoOpinion() {
        Value.Choice choice = new Value.Choice(List.of(new Object(), new Object()), NumberExpr.uniformInt(0, 1));

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, "mod/Trade", "create", CREATE, choice, SELF, Value.UNKNOWN)));
    }

    @Test
    public void testMethodNotReturningOfferHasNoOpinion() {
        Value.Choice choice = new Value.Choice(List.of(new Object(), new Object()), NumberExpr.uniformInt(0, 1));

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEVIRTUAL, "mod/Trade", "name", NAME, choice, SELF)));
    }

    @Test
    public void testNoSubjectHasNoOpinion() {
        context.subject = null;
        assertSame(MethodModel.NO_OPINION, model.apply(context, getOffer(new Value.Known(FIRST))));
    }

    private static Call getOffer(Value listing) {
        RuntimeNames.Ref ref = RuntimeNames.GET_OFFER;

        return call(INVOKEVIRTUAL, LISTING, ref.name(), ref.desc(), listing, Value.UNKNOWN);
    }
}
