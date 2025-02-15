package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinLocationCheck;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class LocationCheckCondition implements ILootCondition {
    public final LocationPredicate predicate;
    public final BlockPos offset;

    public LocationCheckCondition(IContext context, LootItemCondition condition) {
        predicate = ((MixinLocationCheck) condition).getPredicate();
        offset = ((MixinLocationCheck) condition).getOffset();
    }

    public LocationCheckCondition(IContext context, FriendlyByteBuf buf) {
        predicate = LocationPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
        offset = buf.readBlockPos();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
        buf.writeBlockPos(offset);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.location_check")));
        TooltipUtils.addLocationPredicate(components, pad + 1, translatable("ali.property.condition.location_check.location"), predicate);

        if (offset.getX() != 0 && offset.getY() != 0 && offset.getZ() != 0) {
            components.add(pad(pad + 1, translatable("ali.property.condition.location_check.offset")));
            components.add(pad(pad + 2, translatable("ali.property.condition.location_check.x", offset.getX())));
            components.add(pad(pad + 2, translatable("ali.property.condition.location_check.y", offset.getY())));
            components.add(pad(pad + 2, translatable("ali.property.condition.location_check.z", offset.getZ())));
        }

        return components;
    }
}
