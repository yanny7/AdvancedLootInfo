package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

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

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of(pad(pad, translatable("ali.type.function.unknown", functionType)));
    }
}
