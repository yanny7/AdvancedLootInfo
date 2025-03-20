package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinDamageSourceCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class DamageSourcePropertiesAliCondition implements ILootCondition {
    public final DamageSourcePredicate predicate;

    public DamageSourcePropertiesAliCondition(IContext context, LootItemCondition condition) {
        predicate = ((MixinDamageSourceCondition) condition).getPredicate();
    }

    public DamageSourcePropertiesAliCondition(IContext context, FriendlyByteBuf buf) {
        predicate = DamageSourcePredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getDamageSourcePropertiesTooltip(pad, predicate);
    }
}
