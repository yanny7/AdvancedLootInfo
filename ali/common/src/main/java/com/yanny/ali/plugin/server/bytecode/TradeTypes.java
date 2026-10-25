package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

final class TradeTypes {
    private static final Set<String> TRADE_TYPES = Set.of(Type.getInternalName(ItemStack.class), Type.getInternalName(MerchantOffer.class),
            Type.getInternalName(MerchantOffers.class));

    private TradeTypes() {}

    static boolean returnsTradeData(MethodInsnNode call) {
        Type returnType = Type.getReturnType(call.desc);

        return returnType.getSort() == Type.OBJECT && TRADE_TYPES.contains(returnType.getInternalName());
    }

    static boolean isData(@Nullable Object value) {
        return value instanceof Collection<?> || value instanceof Map<?, ?> || value instanceof Tag || value != null && value.getClass().isArray();
    }

    static boolean isSameStack(ItemStack stack, ItemStack other) {
        return ItemStack.isSameItemSameTags(stack, other);
    }

    @NotNull
    static String stackText(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + (stack.hasTag() ? " " + stack.getTag() : "");
    }
}
