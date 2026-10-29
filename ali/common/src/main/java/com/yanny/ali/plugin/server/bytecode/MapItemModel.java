package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

final class MapItemModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();

        if (RuntimeNames.is(RuntimeNames.FIND_MAP_STRUCTURE, insn) || RuntimeNames.is(RuntimeNames.FIND_MAP_STRUCTURE_SET, insn)) {
            return new Value.Unknown(false, Type.getReturnType(insn.desc).getInternalName(), args.get(1) instanceof Value.Known(Object structures) ? target(structures) : null,
                    Origin.of(args.getFirst(), insn.name + insn.desc, args.subList(1, args.size())));
        }
        if (RuntimeNames.is(RuntimeNames.MAP_CREATE, insn)) {
            return new Value.Stack(List.of(new ItemStack(Items.FILLED_MAP)), NumberExpr.constant(1), false);
        }
        if (RuntimeNames.is(RuntimeNames.MAP_RENDER_BIOME_PREVIEW, insn)) {
            return null;
        }
        if (RuntimeNames.is(RuntimeNames.MAP_ADD_TARGET_DECORATION, insn)) {
            if (!(args.getFirst() instanceof Value.Stack stack) || !(args.get(3) instanceof Value.Known(Object decoration))) {
                throw new Fail("map decoration");
            }

            stack.destination = args.get(1) instanceof Value.Unknown pos ? pos.found() : null;
            stack.decoration = decoration;
            return null;
        }

        return NO_OPINION;
    }

    @Nullable
    private static Object target(@Nullable Object structures) {
        return structures instanceof HolderSet<?> set ? set.unwrapKey().<Object>map((tag) -> tag).orElse(set) : structures;
    }
}
