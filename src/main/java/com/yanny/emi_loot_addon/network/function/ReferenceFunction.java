package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinFunctionReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class ReferenceFunction extends LootConditionalFunction {
    public final ResourceLocation name;

    public ReferenceFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        name = ((MixinFunctionReference) function).getName();
    }

    public ReferenceFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        name = buf.readResourceLocation();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(name);
    }
}
