package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.ILootFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class UnknownFunction implements ILootFunction {
    public final ResourceLocation functionType;

    public UnknownFunction(LootContext lootContext, LootItemFunction function) {
        functionType = BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType());
    }

    public UnknownFunction(FriendlyByteBuf buf) {
        functionType = buf.readResourceLocation();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(functionType);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of(pad(pad, translatable("emi.property.function.unknown", functionType)));
    }
}
