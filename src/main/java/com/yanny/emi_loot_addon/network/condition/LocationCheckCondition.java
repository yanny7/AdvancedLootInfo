package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinLocationCheck;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LocationCheckCondition extends LootCondition {
    public final LocationPredicate predicate;
    public final BlockPos offset;

    public LocationCheckCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        predicate = ((MixinLocationCheck) condition).getPredicate();
        offset = ((MixinLocationCheck) condition).getOffset();
    }

    public LocationCheckCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        predicate = LocationPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
        offset = buf.readBlockPos();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
        buf.writeBlockPos(offset);
    }
}
