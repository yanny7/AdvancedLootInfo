package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.ConditionReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class ReferenceAliCondition implements ILootCondition {
    public final ResourceLocation name;

    public ReferenceAliCondition(IContext context, LootItemCondition condition) {
        name = ((ConditionReference) condition).name();
    }

    public ReferenceAliCondition(IContext context, FriendlyByteBuf buf) {
        name = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(name);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getReferenceTooltip(pad, name);
    }
}
