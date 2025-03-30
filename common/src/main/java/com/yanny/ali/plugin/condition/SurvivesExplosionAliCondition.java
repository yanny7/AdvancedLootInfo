package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SurvivesExplosionAliCondition implements ILootCondition {
    public SurvivesExplosionAliCondition(IContext context, LootItemCondition condition) {
    }

    public SurvivesExplosionAliCondition(IContext context, FriendlyByteBuf buf) {
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {}
}
