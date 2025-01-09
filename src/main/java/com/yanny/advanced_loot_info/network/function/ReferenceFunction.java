package com.yanny.advanced_loot_info.network.function;

import com.yanny.advanced_loot_info.mixin.MixinFunctionReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

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

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.function.reference.name", name)));

        return components;
    }
}
