package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class MatchToolCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<ItemPredicate> predicate;

    public MatchToolCondition(IContext context, LootItemCondition condition) {
        predicate = ((MatchTool) condition).predicate();
    }

    public MatchToolCondition(IContext context, FriendlyByteBuf buf) {
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        predicate = jsonElement.flatMap((e) -> ItemPredicate.CODEC.decode(JsonOps.INSTANCE, e).result()).map(Pair::getFirst);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(predicate.flatMap((v) -> ItemPredicate.CODEC.encodeStart(JsonOps.INSTANCE, v).result()),
                (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        predicate.ifPresent((i) -> TooltipUtils.addItemPredicate(components, pad + 1, translatable("ali.type.condition.match_tool"), i));

        return components;
    }
}
