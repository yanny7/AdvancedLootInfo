package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

public class LocationCheckAliCondition implements ILootCondition {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LocationPredicate> predicate;
    public final BlockPos offset;

    public LocationCheckAliCondition(IContext context, LootItemCondition condition) {
        predicate = ((LocationCheck) condition).predicate();
        offset = ((LocationCheck) condition).offset();
    }

    public LocationCheckAliCondition(IContext context, FriendlyByteBuf buf) {
        predicate = buf.readOptional((b) -> LocationPredicate.fromJson(b.readJsonWithCodec(ExtraCodecs.JSON)).orElseThrow());
        offset = buf.readBlockPos();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeOptional(predicate, (b, v) -> b.writeJsonWithCodec(ExtraCodecs.JSON, v.serializeToJson()));
        buf.writeBlockPos(offset);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getLocationCheckTooltip(pad, offset, predicate);
    }
}
