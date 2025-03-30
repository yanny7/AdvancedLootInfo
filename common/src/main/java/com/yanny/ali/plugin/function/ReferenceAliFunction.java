package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinFunctionReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class ReferenceAliFunction extends LootConditionalAliFunction {
    public final ResourceLocation name;

    public ReferenceAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinFunctionReference) function).getName();
    }

    public ReferenceAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(name);
    }
}
