package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinLocationCheck;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LocationCheckAliCondition implements ILootCondition {
    public final LocationPredicate predicate;
    public final BlockPos offset;

    public LocationCheckAliCondition(IContext context, LootItemCondition condition) {
        predicate = ((MixinLocationCheck) condition).getPredicate();
        offset = ((MixinLocationCheck) condition).getOffset();
    }

    public LocationCheckAliCondition(IContext context, FriendlyByteBuf buf) {
        predicate = LocationPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
        offset = buf.readBlockPos();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
        buf.writeBlockPos(offset);
    }
}
