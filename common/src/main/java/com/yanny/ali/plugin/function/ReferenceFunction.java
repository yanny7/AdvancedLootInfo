package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinFunctionReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class ReferenceFunction extends LootConditionalFunction {
    public final ResourceKey<LootTable> name;

    public ReferenceFunction(IContext context, LootItemFunction function) {
        super(context, function);
        name = ((MixinFunctionReference) function).getName();
    }

    public ReferenceFunction(IContext context, FriendlyByteBuf buf) {
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.reference")));
        components.add(pad(pad + 1, translatable("ali.property.function.reference.name", name.location())));

        return components;
    }
}
