package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinItemEntityPropertyCondition;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class EntityPropertiesAliCondition implements ILootCondition {
    public final LootContext.EntityTarget target;
    public final EntityPredicate predicate;

    public EntityPropertiesAliCondition(IContext context, LootItemCondition condition) {
        target = ((MixinItemEntityPropertyCondition) condition).getEntityTarget();
        predicate = ((MixinItemEntityPropertyCondition) condition).getPredicate();
    }

    public EntityPropertiesAliCondition(IContext context, FriendlyByteBuf buf) {
        target = buf.readEnum(LootContext.EntityTarget.class);
        predicate = EntityPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeEnum(target);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return ConditionTooltipUtils.getEntityPropertiesTooltip(pad, target, predicate);
    }
}
