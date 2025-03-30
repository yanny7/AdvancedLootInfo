package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class UnknownAliFunction implements ILootFunction {
    public final ResourceLocation functionType;

    public UnknownAliFunction(IContext context, LootItemFunction function) {
        functionType = BuiltInRegistries.LOOT_FUNCTION_TYPE.getKey(function.getType());
    }

    public UnknownAliFunction(IContext context, FriendlyByteBuf buf) {
        functionType = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(functionType);
    }
}
