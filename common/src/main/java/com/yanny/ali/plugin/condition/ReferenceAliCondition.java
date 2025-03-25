package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.ConditionReference;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class ReferenceAliCondition implements ILootCondition {
    public final ResourceKey<LootItemCondition> name;

    public ReferenceAliCondition(IContext context, LootItemCondition condition) {
        name = ((ConditionReference) condition).name();
    }

    public ReferenceAliCondition(IContext context, FriendlyByteBuf buf) {
        name = buf.readResourceKey(Registries.PREDICATE);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeResourceKey(name);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getReferenceTooltip(pad, name);
    }
}
