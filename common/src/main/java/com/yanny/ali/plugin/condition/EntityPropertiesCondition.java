package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class EntityPropertiesCondition implements ILootCondition {
    public final LootContext.EntityTarget target;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<EntityPredicate> predicate;

    public EntityPropertiesCondition(IContext context, LootItemCondition condition) {
        target = ((LootItemEntityPropertyCondition) condition).entityTarget();
        predicate = ((LootItemEntityPropertyCondition) condition).predicate();
    }

    public EntityPropertiesCondition(IContext context, FriendlyByteBuf buf) {
        target = buf.readEnum(LootContext.EntityTarget.class);
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        predicate = jsonElement.flatMap((e) -> EntityPredicate.CODEC.decode(JsonOps.INSTANCE, e).result()).map(Pair::getFirst);

    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeEnum(target);
        buf.writeOptional(predicate.flatMap((v) -> EntityPredicate.CODEC.encodeStart(JsonOps.INSTANCE, v).result()),
                (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        predicate.ifPresent((e) -> {
            components.add(pad(pad, translatable("ali.type.condition.entity_properties")));
            addEntityPredicate(components, pad + 1, translatable("ali.property.condition.predicate.target", value(translatableType("ali.enum.target", target))), e);
        });

        return components;
    }
}
