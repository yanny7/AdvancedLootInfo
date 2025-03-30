package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinDamageSourceCondition;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

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
}
