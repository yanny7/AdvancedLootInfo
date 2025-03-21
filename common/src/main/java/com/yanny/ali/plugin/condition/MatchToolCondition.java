package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.List;
import java.util.Optional;

public class MatchToolCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<ItemPredicate> predicate;

    public MatchToolCondition(IContext context, LootItemCondition condition) {
        predicate = ((MatchTool) condition).predicate();
    }

    public MatchToolCondition(IContext context, FriendlyByteBuf buf) {
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        predicate = jsonElement.flatMap(ItemPredicate::fromJson);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(predicate, (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v.serializeToJson()));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getMatchToolTooltip(pad, predicate);
    }
}
