package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

final class TradeTypes {
    private static final Set<String> TRADE_TYPES = Set.of(Type.getInternalName(ItemStack.class), Type.getInternalName(ItemCost.class),
            Type.getInternalName(MerchantOffer.class), Type.getInternalName(MerchantOffers.class));

    private TradeTypes() {}

    static boolean returnsTradeData(MethodInsnNode call) {
        Type returnType = Type.getReturnType(call.desc);

        return returnType.getSort() == Type.OBJECT && TRADE_TYPES.contains(returnType.getInternalName());
    }

    static boolean isObject(Type type, String internalName) {
        return type.getSort() == Type.OBJECT && type.getInternalName().equals(internalName);
    }

    static boolean isData(@Nullable Object value) {
        return value instanceof Collection<?> || value instanceof Map<?, ?> || value instanceof Tag || value instanceof DataComponentMap || value instanceof DataComponentPatch
                || value != null && value.getClass().isArray();
    }
}
