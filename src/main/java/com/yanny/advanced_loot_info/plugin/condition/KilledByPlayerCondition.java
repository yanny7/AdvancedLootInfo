package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.ILootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class KilledByPlayerCondition implements ILootCondition {
    public KilledByPlayerCondition(LootContext lootContext, LootItemCondition condition) {
    }

    public KilledByPlayerCondition(FriendlyByteBuf buf) {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {

    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.killed_by_player")));

        return components;
    }
}
