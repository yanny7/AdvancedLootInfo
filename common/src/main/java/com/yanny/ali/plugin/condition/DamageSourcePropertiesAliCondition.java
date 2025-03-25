package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

public class DamageSourcePropertiesAliCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<DamageSourcePredicate> predicate;

    public DamageSourcePropertiesAliCondition(IContext context, LootItemCondition condition) {
        predicate = ((DamageSourceCondition) condition).predicate();
    }

    public DamageSourcePropertiesAliCondition(IContext context, FriendlyByteBuf buf) {
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        predicate = jsonElement.flatMap((e) -> DamageSourcePredicate.CODEC.decode(JsonOps.INSTANCE, e).result()).map(Pair::getFirst);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(predicate.flatMap((v) -> DamageSourcePredicate.CODEC.encodeStart(JsonOps.INSTANCE, v).result()),
                (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getDamageSourcePropertiesTooltip(pad, predicate);
    }
}
