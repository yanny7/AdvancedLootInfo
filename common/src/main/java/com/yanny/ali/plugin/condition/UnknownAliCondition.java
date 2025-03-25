package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

import static com.yanny.ali.plugin.GenericTooltipUtils.pad;
import static com.yanny.ali.plugin.GenericTooltipUtils.translatable;

public class UnknownAliCondition implements ILootCondition {
    public final ResourceLocation conditionType;

    public UnknownAliCondition(IContext context, LootItemCondition condition) {
        conditionType = BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType());
    }

    public UnknownAliCondition(IContext context, FriendlyByteBuf buf) {
        conditionType = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(conditionType);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of(pad(pad, translatable("ali.type.condition.unknown", conditionType)));
    }
}
