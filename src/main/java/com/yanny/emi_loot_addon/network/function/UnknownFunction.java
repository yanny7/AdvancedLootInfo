package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.network.LootFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.pad;
import static com.yanny.emi_loot_addon.compatibility.EmiUtils.translatable;

public class UnknownFunction extends LootFunction {
    public final ResourceLocation functionType;

    public UnknownFunction(LootContext lootContext, LootItemFunction function) {
        super(FunctionType.of(function.getType()));
        functionType = BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType());
    }

    public UnknownFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type);
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
