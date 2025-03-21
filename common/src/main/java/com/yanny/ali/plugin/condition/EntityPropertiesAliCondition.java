package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.List;
import java.util.Optional;

public class EntityPropertiesAliCondition implements ILootCondition {
    public final LootContext.EntityTarget target;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<EntityPredicate> predicate;

    public EntityPropertiesAliCondition(IContext context, LootItemCondition condition) {
        target = ((LootItemEntityPropertyCondition) condition).entityTarget();
        predicate = ((LootItemEntityPropertyCondition) condition).predicate();
    }

    public EntityPropertiesAliCondition(IContext context, FriendlyByteBuf buf) {
        target = buf.readEnum(LootContext.EntityTarget.class);
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        predicate = jsonElement.flatMap(EntityPredicate::fromJson);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeEnum(target);
        buf.writeOptional(predicate, (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v.serializeToJson()));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getEntityPropertiesTooltip(pad, target, predicate);
    }
}
