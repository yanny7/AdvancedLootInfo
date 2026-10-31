package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.npc.villager.AbstractVillager;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;

public class FieldModelTest {
    private static final String HOLDER = Type.getInternalName(Holder.class);
    private static final Value.Unknown SELF = Value.Unknown.of(Type.getType(Holder.class), Origin.SELF);
    private static final ClassLoader LOADER = FieldModelTest.class.getClassLoader();

    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testVillagerOffersOfTraderAreSymbolicOffers() {
        TraderSubject run = run(false);

        assertSame(run.offers, get(offers(GETFIELD), SELF));
    }

    @Test
    public void testVillagerOffersWithOwnGetOffersAreNullUntilWritten() {
        run(true);

        assertSame(Interpreter.NULL, get(offers(GETFIELD), SELF));
    }

    @Test
    public void testWrittenTraderFieldReadsBack() {
        Value written = new Value.Known(3);

        run(false);
        context.selfFields.put(HOLDER + ".count", written);
        assertSame(written, get(field(GETFIELD, "count", "I"), SELF));
    }

    @Test
    public void testRandomFieldOfUnknownIsRandomSource() {
        assertEquals(new Value.Rng(false), get(field(GETFIELD, "random", "Lnet/minecraft/util/RandomSource;"), unknown()));
    }

    @Test
    public void testFieldOfUnknownIsUnknownWithOrigin() {
        Value.Unknown receiver = unknown();
        Value.Unknown value = assertInstanceOf(Value.Unknown.class, get(field(GETFIELD, "count", "I"), receiver));

        assertEquals(Origin.of(receiver, "count", List.of()), value.origin());
    }

    @Test
    public void testFieldOfChoiceIsReadPerOption() {
        Holder first = new Holder(1);
        Holder second = new Holder(2);
        Value combined = new Value.Known(5);

        context.combineResult = combined;
        assertSame(combined, get(field(GETFIELD, "count", "I"), new Value.Choice(List.of(first, second))));
        assertEquals(List.of(List.of(new Value.Known(1), new Value.Known(2))), context.combined);
    }

    @Test
    public void testFieldOfUncertainObjectTaintsItsData() {
        Holder holder = new Holder(1);

        context.tainted.add(holder);
        assertSame(holder.items, ((Value.Known) get(field(GETFIELD, "items", "Ljava/util/List;"), new Value.Known(holder))).value());
        assertTrue(context.tainted.contains(holder.items));
    }

    @Test
    public void testFieldOfSymbolicValueFails() {
        assertEquals("field of a symbolic value", assertThrows(Fail.class, () -> get(field(GETFIELD, "count", "I"), Interpreter.NULL)).getMessage());
    }

    @Test
    public void testOffersWrittenToTraderReplaceSymbolicOffers() {
        TraderSubject run = run(false);
        Value.Offers offers = new Value.Offers();

        FieldModel.put(context, offers(PUTFIELD), SELF, offers, LOADER);
        assertSame(offers, run.offers);
        assertTrue(context.selfFields.isEmpty());
    }

    @Test
    public void testOffersWrittenToTraderWithOwnGetOffersAreAlsoAField() {
        Value.Offers offers = new Value.Offers();

        run(true);
        FieldModel.put(context, offers(PUTFIELD), SELF, offers, LOADER);
        assertEquals(1, context.selfFields.size());
        assertSame(offers, context.selfFields.values().iterator().next());
    }

    @Test
    public void testOtherTraderFieldIsWrittenToStore() {
        Value value = new Value.Known(4);

        run(false);
        FieldModel.put(context, field(PUTFIELD, "count", "I"), SELF, value, LOADER);
        assertSame(value, context.selfFields.get(HOLDER + ".count"));
    }

    @Test
    public void testFieldOfOwnedObjectIsWritten() {
        Holder holder = new Holder(1);

        context.owned.add(holder);
        FieldModel.put(context, field(PUTFIELD, "count", "I"), new Value.Known(holder), new Value.Known(7), LOADER);
        assertEquals(7, holder.count);
    }

    @Test
    public void testFieldOfForeignObjectIsNotWritten() {
        Holder holder = new Holder(1);

        assertEquals("write to a field", assertThrows(Fail.class,
                () -> FieldModel.put(context, field(PUTFIELD, "count", "I"), new Value.Known(holder), new Value.Known(7), LOADER)).getMessage());
        assertEquals(1, holder.count);
    }

    @Test
    public void testFieldOfFrozenObjectIsNotWritten() {
        Holder holder = new Holder(1);

        context.owned.add(holder);
        context.frozen.add(holder);
        assertEquals("change under a random choice", assertThrows(Fail.class,
                () -> FieldModel.put(context, field(PUTFIELD, "count", "I"), new Value.Known(holder), new Value.Known(7), LOADER)).getMessage());
    }

    private Value get(FieldInsnNode insn, Value receiver) {
        return FieldModel.get(context, insn, receiver, LOADER);
    }

    private TraderSubject run(boolean ownGetOffers) {
        TraderSubject run = new TraderSubject(Holder.class, ownGetOffers);

        context.subject = run;
        return run;
    }

    private static Value.Unknown unknown() {
        return Value.Unknown.of(Type.getType(Holder.class), Origin.of(Value.Unknown.of(Type.getType(Holder.class), Origin.SELF), "holder", List.of()));
    }

    private static FieldInsnNode offers(int opcode) {
        RuntimeNames.Ref ref = RuntimeNames.OFFERS;

        return new FieldInsnNode(opcode, Type.getInternalName(AbstractVillager.class), ref.name(), ref.desc());
    }

    private static FieldInsnNode field(int opcode, String name, String desc) {
        return new FieldInsnNode(opcode, HOLDER, name, desc);
    }

    public static class Holder {
        public int count;
        public final List<Object> items = new ArrayList<>();

        public Holder(int count) {
            this.count = count;
        }
    }
}
