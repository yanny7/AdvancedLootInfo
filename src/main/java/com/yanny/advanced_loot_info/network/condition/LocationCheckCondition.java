package com.yanny.advanced_loot_info.network.condition;

import com.yanny.advanced_loot_info.mixin.MixinLocationCheck;
import com.yanny.advanced_loot_info.network.LootCondition;
import com.yanny.advanced_loot_info.network.TooltipUtils;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

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

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        TooltipUtils.addLocationPredicate(components, pad + 1, translatable("emi.property.condition.location_check.location"), predicate);

        if (offset.getX() != 0 && offset.getY() != 0 && offset.getZ() != 0) {
            components.add(pad(pad + 1, translatable("emi.property.condition.location_check.offset")));
            components.add(pad(pad + 2, translatable("emi.property.condition.location_check.x", offset.getX())));
            components.add(pad(pad + 2, translatable("emi.property.condition.location_check.y", offset.getY())));
            components.add(pad(pad + 2, translatable("emi.property.condition.location_check.z", offset.getZ())));
        }

        return components;
    }
}
