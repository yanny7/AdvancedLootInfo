package com.yanny.ali.plugin.condition;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class LocationCheckCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LocationPredicate> predicate;
    public final BlockPos offset;

    public LocationCheckCondition(IContext context, LootItemCondition condition) {
        predicate = ((LocationCheck) condition).predicate();
        offset = ((LocationCheck) condition).offset();
    }

    public LocationCheckCondition(IContext context, FriendlyByteBuf buf) {
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));

        predicate = jsonElement.flatMap((e) -> LocationPredicate.CODEC.decode(JsonOps.INSTANCE, e).result()).map(Pair::getFirst);
        offset = buf.readBlockPos();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(predicate.flatMap((v) -> LocationPredicate.CODEC.encodeStart(JsonOps.INSTANCE, v).result()),
                (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v));
        buf.writeBlockPos(offset);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.location_check")));
        predicate.ifPresent((l) -> addLocationPredicate(components, pad + 1, translatable("ali.property.condition.location_check.location"), l));

        if (offset.getX() != 0 && offset.getY() != 0 && offset.getZ() != 0) {
            components.add(pad(pad + 1, translatable("ali.property.condition.location_check.offset")));
            components.add(pad(pad + 2, translatable("ali.property.condition.location_check.x", offset.getX())));
            components.add(pad(pad + 2, translatable("ali.property.condition.location_check.y", offset.getY())));
            components.add(pad(pad + 2, translatable("ali.property.condition.location_check.z", offset.getZ())));
        }

        return components;
    }
}
