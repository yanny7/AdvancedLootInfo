package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class KilledByPlayerAliCondition implements ILootCondition {
    public KilledByPlayerAliCondition(IContext context, LootItemCondition condition) {
    }

    public KilledByPlayerAliCondition(IContext context, FriendlyByteBuf buf) {
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {

    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getKilledByPlayerTooltip(pad);
    }
}
