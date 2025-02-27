package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class DamageSourcePropertiesCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<DamageSourcePredicate> predicate;

    public DamageSourcePropertiesCondition(IContext context, LootItemCondition condition) {
        predicate = ((DamageSourceCondition) condition).predicate();
    }

    public DamageSourcePropertiesCondition(IContext context, FriendlyByteBuf buf) {
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        predicate = jsonElement.flatMap(DamageSourcePredicate::fromJson);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(predicate, (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v.serializeToJson()));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        predicate.ifPresent((p) -> TooltipUtils.addDamageSourcePredicate(components, pad, translatable("ali.type.condition.damage_source_properties"), p));

        return components;
    }
}
