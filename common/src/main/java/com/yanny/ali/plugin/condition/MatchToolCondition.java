package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinMatchTool;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class MatchToolCondition implements ILootCondition {
    public final ItemPredicate predicate;

    public MatchToolCondition(IContext context, LootItemCondition condition) {
        predicate = ((MixinMatchTool) condition).getPredicate();
    }

    public MatchToolCondition(IContext context, FriendlyByteBuf buf) {
        predicate = ItemPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getMatchToolTooltip(pad, predicate);
    }
}
