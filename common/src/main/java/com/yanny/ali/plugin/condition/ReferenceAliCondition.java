package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinConditionReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ReferenceAliCondition implements ILootCondition {
    public final ResourceLocation name;

    public ReferenceAliCondition(IContext context, LootItemCondition condition) {
        name = ((MixinConditionReference) condition).getName();
    }

    public ReferenceAliCondition(IContext context, FriendlyByteBuf buf) {
        name = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceLocation(name);
    }
}
