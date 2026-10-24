package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class MapItemModelTest {
    private static final Value.Unknown LEVEL = new Value.Unknown(false, "net/minecraft/server/level/ServerLevel", null, Origin.TRADER);

    private final MapItemModel model = new MapItemModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testFindMapStructureIsPositionKnownByTarget() {
        Object target = new Object();
        List<Value> rest = List.of(new Value.Known(target), new Value.Known(BlockPos.ZERO), new Value.Known(100), new Value.Known(1));
        Call call = mapCall(RuntimeNames.FIND_MAP_STRUCTURE, INVOKEVIRTUAL, LEVEL, rest.get(0), rest.get(1), rest.get(2), rest.get(3));
        Value result = model.apply(context, call);

        assertEquals(new Value.Unknown(false, "net/minecraft/core/BlockPos", target, Origin.of(LEVEL, call.insn().name + call.insn().desc, rest)), result);
    }

    @Test
    public void testCreateIsFilledMap() {
        Value result = model.apply(context, mapCall(RuntimeNames.MAP_CREATE, INVOKESTATIC, Value.UNKNOWN, new Value.Known(0), new Value.Known(0), new Value.Known(2),
                new Value.Known(1), new Value.Known(1)));
        Value.Stack stack = assertInstanceOf(Value.Stack.class, result);

        assertEquals(1, stack.options.size());
        assertTrue(stack.options.get(0).is(Items.FILLED_MAP));
        assertEquals(NumberExpr.constant(1), stack.count);
        assertFalse(stack.randomTag);
    }

    @Test
    public void testRenderBiomePreviewDoesNothing() {
        assertNull(model.apply(context, mapCall(RuntimeNames.MAP_RENDER_BIOME_PREVIEW, INVOKESTATIC, Value.UNKNOWN, new Value.Known(null))));
    }

    @Test
    public void testAddTargetDecorationWritesStack() {
        Value.Stack stack = new Value.Stack(List.of(new ItemStack(Items.FILLED_MAP)), NumberExpr.constant(1), false);
        BlockPos pos = BlockPos.ZERO;
        Value.Unknown found = new Value.Unknown(false, "net/minecraft/core/BlockPos", pos, Origin.TRADER);

        assertNull(model.apply(context, mapCall(RuntimeNames.MAP_ADD_TARGET_DECORATION, INVOKESTATIC, stack, found, new Value.Known("+"),
                new Value.Known(MapDecorationTypes.RED_X))));
        assertSame(pos, stack.destination);
        assertSame(MapDecorationTypes.RED_X, stack.decoration);
    }

    @Test
    public void testAddTargetDecorationWithConcretePositionClearsDestination() {
        Value.Stack stack = new Value.Stack(List.of(new ItemStack(Items.FILLED_MAP)), NumberExpr.constant(1), false);

        stack.destination = BlockPos.ZERO;
        assertNull(model.apply(context, mapCall(RuntimeNames.MAP_ADD_TARGET_DECORATION, INVOKESTATIC, stack, new Value.Known(BlockPos.ZERO), new Value.Known("+"),
                new Value.Known(MapDecorationTypes.RED_X))));
        assertNull(stack.destination);
    }

    @Test
    public void testAddTargetDecorationOnRealStackFails() {
        Call call = mapCall(RuntimeNames.MAP_ADD_TARGET_DECORATION, INVOKESTATIC, new Value.Known(new ItemStack(Items.FILLED_MAP)), Value.UNKNOWN, new Value.Known("+"),
                new Value.Known(MapDecorationTypes.RED_X));

        assertEquals("map decoration", assertThrows(Fail.class, () -> model.apply(context, call)).getMessage());
    }

    @Test
    public void testOtherCallHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, call(INVOKESTATIC, "net/minecraft/world/item/MapItem", "getMapId", "(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/Integer;",
                new Value.Known(null))));
    }

    private static Call mapCall(RuntimeNames.Ref ref, int opcode, Value... args) {
        return call(opcode, "net/minecraft/Owner", ref.name(), ref.desc(), args);
    }
}
