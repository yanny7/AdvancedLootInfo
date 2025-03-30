package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinMatchTool;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class MatchToolAliCondition implements ILootCondition {
    public final ItemPredicate predicate;

    public MatchToolAliCondition(IContext context, LootItemCondition condition) {
        predicate = ((MixinMatchTool) condition).getPredicate();
    }

    public MatchToolAliCondition(IContext context, FriendlyByteBuf buf) {
        predicate = ItemPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }
}
