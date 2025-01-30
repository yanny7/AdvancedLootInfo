package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class UnknownCondition implements ILootCondition {
    public final ResourceLocation conditionType;

    public UnknownCondition(IContext context, LootItemCondition condition) {
        conditionType = BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType());
    }

    public UnknownCondition(IContext context, FriendlyByteBuf buf) {
        conditionType = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(conditionType);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.property.condition.unknown", conditionType)));

        return components;
    }
}
