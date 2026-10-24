package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class CollectionsModelTest {
    private static final String LIST = "java/util/List";
    private static final String SET = "java/util/Set";
    private static final String COLLECTIONS = "java/util/Collections";

    private final CollectionsModel model = new CollectionsModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testGetAtUniformIndexIsChoice() {
        NumberExpr index = NumberExpr.uniformInt(0, 2);
        List<String> list = List.of("a", "b", "c", "d");

        assertEquals(new Value.Choice(List.of("a", "b", "c"), index), model.apply(context, get(list, new Value.Num(index, Value.Kind.INT))));
    }

    @Test
    public void testGetAtIndexPastEndFails() {
        Value.Num index = new Value.Num(NumberExpr.uniformInt(0, 4), Value.Kind.INT);

        assertEquals("index out of range", assertThrows(Fail.class, () -> model.apply(context, get(List.of("a", "b"), index))).getMessage());
    }

    @Test
    public void testRemoveAtRandomIndexIsPoolChoice() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b", "c")));
        Value result = model.apply(context, remove(list, new Value.Num(NumberExpr.uniformInt(0, 2), Value.Kind.INT)));

        assertEquals(new Value.Choice(List.of("a", "b", "c"), list), result);
        assertEquals(1, context.removals(list));
        assertTrue(context.coll(list).pool);
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    public void testRemoveAtIndexNotCoveringRemainingFails() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b", "c")));
        Value.Num index = new Value.Num(NumberExpr.uniformInt(0, 1), Value.Kind.INT);

        assertEquals("removal of a random element", assertThrows(Fail.class, () -> model.apply(context, remove(list, index))).getMessage());
    }

    @Test
    public void testRemoveAfterRemovalCountsRemaining() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b", "c")));

        context.coll(list).removals = 1;
        assertInstanceOf(Value.Choice.class, model.apply(context, remove(list, new Value.Num(NumberExpr.uniformInt(0, 1), Value.Kind.INT))));
        assertEquals(2, context.removals(list));
    }

    @Test
    public void testRemoveFromSingleListingOfTraderIsPick() {
        List<String> list = owned(new ArrayList<>(List.of("a")));

        context.traderRun = true;
        assertEquals(new Value.Choice(List.of("a"), list), model.apply(context, remove(list, new Value.Known(0))));
    }

    @Test
    public void testRemoveAtConcreteIndexHasNoOpinion() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b")));

        assertSame(MethodModel.NO_OPINION, model.apply(context, remove(list, new Value.Known(0))));
    }

    @Test
    public void testRemoveFromListNotOwnedHasNoOpinion() {
        List<String> list = new ArrayList<>(List.of("a", "b"));

        assertSame(MethodModel.NO_OPINION, model.apply(context, remove(list, new Value.Num(NumberExpr.uniformInt(0, 1), Value.Kind.INT))));
    }

    @Test
    public void testShuffleOwnedListMakesShuffledPool() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b")));

        assertNull(model.apply(context, call(INVOKESTATIC, COLLECTIONS, "shuffle", "(Ljava/util/List;)V", new Value.Known(list))));

        Value.Draws draws = context.shuffled(list);

        assertNotNull(draws);
        assertSame(list, draws.pool);
        assertEquals(2, draws.left);
        assertTrue(context.coll(list).pool);
    }

    @Test
    public void testShuffleEmptyListChangesNothing() {
        List<String> list = owned(new ArrayList<>());

        assertNull(model.apply(context, call(INVOKESTATIC, COLLECTIONS, "shuffle", "(Ljava/util/List;)V", new Value.Known(list))));
        assertNull(context.shuffled(list));
    }

    @Test
    public void testShuffledSizeAndGet() {
        List<String> list = shuffled(List.of("a", "b", "c"));

        assertEquals(new Value.Known(3), model.apply(context, call(INVOKEINTERFACE, LIST, "size", "()I", new Value.Known(list))));
        assertEquals(new Value.Choice(List.of("a", "b", "c"), list), model.apply(context, get(list, new Value.Known(1))));
    }

    @Test
    public void testShuffledSubListByRandomCountIsDrawCount() {
        List<String> list = shuffled(List.of("a", "b", "c"));
        NumberExpr count = NumberExpr.uniformInt(1, 2);
        Value result = model.apply(context, call(INVOKEINTERFACE, LIST, "subList", "(II)Ljava/util/List;", new Value.Known(list), new Value.Known(0), new Value.Num(count, Value.Kind.INT)));
        Object view = ((Value.Known) Objects.requireNonNull(result)).value();

        assertEquals(count, context.coll(list).drawCount);
        assertTrue(context.owned.contains(view));
        assertEquals(2, Objects.requireNonNull(context.shuffled(view)).left);
    }

    @Test
    public void testShuffledChangeFails() {
        List<String> list = shuffled(List.of("a", "b"));
        Value.Known receiver = new Value.Known(list);

        assertEquals("change under a random choice", assertThrows(Fail.class,
                () -> model.apply(context, call(INVOKEINTERFACE, LIST, "add", "(Ljava/lang/Object;)Z", receiver, new Value.Known("c")))).getMessage());
    }

    @Test
    public void testAddRandomIndexFillsRandomIndexSet() {
        Set<Integer> set = owned(new HashSet<>());
        Value.Num index = new Value.Num(NumberExpr.uniformInt(0, 4), Value.Kind.INT);

        assertEquals(new Value.Known(1), model.apply(context, add(set, index)));
        assertEquals(new Value.Known(1), model.apply(context, add(set, index)));
        assertArrayEquals(new int[]{5, 2}, context.randomIndexSet(set));
        assertTrue(set.isEmpty());
    }

    @Test
    public void testAddRandomIndexWithOtherBoundFails() {
        Set<Integer> set = owned(new HashSet<>());

        model.apply(context, add(set, new Value.Num(NumberExpr.uniformInt(0, 4), Value.Kind.INT)));
        assertEquals("random set element", assertThrows(Fail.class, () -> model.apply(context, add(set, new Value.Num(NumberExpr.uniformInt(0, 3), Value.Kind.INT)))).getMessage());
    }

    @Test
    public void testRandomIndexSetSizeAndIterator() {
        Set<Integer> set = owned(new HashSet<>());

        context.coll(set).randomIndexSet = new int[]{3, 2};
        assertEquals(new Value.Known(2), model.apply(context, call(INVOKEINTERFACE, SET, "size", "()I", new Value.Known(set))));

        Value.Draws draws = (Value.Draws) model.apply(context, call(INVOKEINTERFACE, SET, "iterator", "()Ljava/util/Iterator;", new Value.Known(set)));

        assertNotNull(draws);
        assertEquals(List.of(0, 1, 2), draws.options);
        assertEquals(2, draws.left);
    }

    @Test
    public void testRemovedSizeCountsRemaining() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b", "c")));

        context.coll(list).removals = 2;
        assertEquals(new Value.Known(1), model.apply(context, call(INVOKEINTERFACE, LIST, "size", "()I", new Value.Known(list))));
        assertEquals(new Value.Known(0), model.apply(context, call(INVOKEINTERFACE, LIST, "isEmpty", "()Z", new Value.Known(list))));
    }

    @Test
    public void testRemovedReadFails() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b")));

        context.coll(list).removals = 1;
        assertEquals("change under a random choice", assertThrows(Fail.class, () -> model.apply(context, get(list, new Value.Known(0)))).getMessage());
    }

    @Test
    public void testRemoveWholeRemainingChoiceCountsRemoval() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b")));
        Value result = CollectionsModel.REMOVE_CHOICE.apply(context, call(INVOKEINTERFACE, LIST, "remove", "(Ljava/lang/Object;)Z", new Value.Known(list), new Value.Choice(List.of("a", "b"))));

        assertEquals(new Value.Known(1), result);
        assertEquals(1, context.removals(list));
        assertEquals(List.of("a", "b"), list);
    }

    @Test
    public void testRemovePartialChoiceHasNoOpinion() {
        List<String> list = owned(new ArrayList<>(List.of("a", "b", "c")));
        Value result = CollectionsModel.REMOVE_CHOICE.apply(context, call(INVOKEINTERFACE, LIST, "remove", "(Ljava/lang/Object;)Z", new Value.Known(list), new Value.Choice(List.of("a", "b"))));

        assertSame(MethodModel.NO_OPINION, result);
    }

    @Test
    public void testPlainListHasNoOpinion() {
        List<String> list = owned(new ArrayList<>(List.of("a")));

        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKEINTERFACE, LIST, "size", "()I", new Value.Known(list))));
    }

    private <T> T owned(T object) {
        context.own(object);
        return object;
    }

    private List<String> shuffled(List<String> elements) {
        List<String> list = owned(new ArrayList<>(elements));

        model.apply(context, call(INVOKESTATIC, COLLECTIONS, "shuffle", "(Ljava/util/List;)V", new Value.Known(list)));
        return list;
    }

    private static Call get(List<?> list, Value index) {
        return call(INVOKEINTERFACE, LIST, "get", "(I)Ljava/lang/Object;", new Value.Known(list), index);
    }

    private static Call remove(List<?> list, Value index) {
        return call(INVOKEINTERFACE, LIST, "remove", "(I)Ljava/lang/Object;", new Value.Known(list), index);
    }

    private static Call add(Set<?> set, Value element) {
        return call(INVOKEINTERFACE, SET, "add", "(Ljava/lang/Object;)Z", new Value.Known(set), element);
    }
}
