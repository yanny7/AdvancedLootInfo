package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SurvivesExplosionCondition implements ILootCondition {
    public SurvivesExplosionCondition(IContext context, LootItemCondition condition) {
    }

    public SurvivesExplosionCondition(IContext context, FriendlyByteBuf buf) {
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {}

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.survives_explosion")));

        return components;
    }
}
