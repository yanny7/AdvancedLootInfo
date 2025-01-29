package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.manager.PluginManager;
import com.yanny.advanced_loot_info.mixin.MixinInvertedLootItemCondition;
import com.yanny.advanced_loot_info.network.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class InvertedCondition implements ILootCondition {
    public final ILootCondition term;

    public InvertedCondition(LootContext lootContext, LootItemCondition condition) {
        LootItemCondition termCondition = ((MixinInvertedLootItemCondition) condition).getTerm();
        term = PluginManager.REGISTRY.convertCondition(lootContext, termCondition);
    }

    public InvertedCondition(FriendlyByteBuf buf) {
        term = PluginManager.REGISTRY.decodeCondition(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        PluginManager.REGISTRY.encodeCondition(buf, term);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.inverted")));
        components.addAll(TooltipUtils.getConditions(List.of(term), pad + 1));

        return components;
    }
}
