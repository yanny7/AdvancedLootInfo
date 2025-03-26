package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinFunctionReference;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class ReferenceAliFunction extends LootConditionalAliFunction {
    public final ResourceKey<LootTable> name;

    public ReferenceAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinFunctionReference) function).getName();
    }

    public ReferenceAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = buf.readResourceKey(Registries.LOOT_TABLE);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceKey(name);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getReferenceTooltip(pad, name);
    }
}
