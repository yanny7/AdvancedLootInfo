package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinItemEntityPropertyCondition;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntityPropertiesCondition extends LootCondition {
    public final LootContext.EntityTarget target;
    public final EntityPredicate predicate;

    public EntityPropertiesCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        target = ((MixinItemEntityPropertyCondition) condition).getEntityTarget();
        predicate = ((MixinItemEntityPropertyCondition) condition).getPredicate();
    }

    public EntityPropertiesCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        target = buf.readEnum(LootContext.EntityTarget.class);
        predicate = EntityPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(target);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }
}
