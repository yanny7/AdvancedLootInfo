package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinDamageSourceCondition;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class DamageSourcePropertiesCondition implements ILootCondition {
    public final DamageSourcePredicate predicate;

    public DamageSourcePropertiesCondition(IContext context, LootItemCondition condition) {
        predicate = ((MixinDamageSourceCondition) condition).getPredicate();
    }

    public DamageSourcePropertiesCondition(IContext context, FriendlyByteBuf buf) {
        predicate = DamageSourcePredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        TooltipUtils.addDamageSourcePredicate(components, pad, translatable("ali.type.condition.damage_source_properties"), predicate);

        return components;
    }
}
