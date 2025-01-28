package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.manager.PluginManager;
import com.yanny.advanced_loot_info.mixin.MixinInvertedLootItemCondition;
import com.yanny.advanced_loot_info.network.TooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class InvertedCondition implements ILootCondition {
    public final ResourceLocation termType;
    public final ILootCondition term;

    public InvertedCondition(LootContext lootContext, LootItemCondition condition) {
        LootItemCondition termCondition = ((MixinInvertedLootItemCondition) condition).getTerm();
        termType = BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(termCondition.getType());
        term = PluginManager.REGISTRY.getCondition(lootContext, termCondition);
    }

    public InvertedCondition(FriendlyByteBuf buf) {
        termType = buf.readResourceLocation();
        term = PluginManager.REGISTRY.getCondition(termType, buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(termType);
        term.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.inverted")));
        components.addAll(TooltipUtils.getConditions(List.of(term), pad + 1));

        return components;
    }
}
