package com.yanny.advanced_loot_info.plugin.condition;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.mixin.MixinItemEntityPropertyCondition;
import com.yanny.advanced_loot_info.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class EntityPropertiesCondition implements ILootCondition {
    public final LootContext.EntityTarget target;
    public final EntityPredicate predicate;

    public EntityPropertiesCondition(IContext context, LootItemCondition condition) {
        target = ((MixinItemEntityPropertyCondition) condition).getEntityTarget();
        predicate = ((MixinItemEntityPropertyCondition) condition).getPredicate();
    }

    public EntityPropertiesCondition(IContext context, FriendlyByteBuf buf) {
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.condition.entity_properties")));
        TooltipUtils.addEntityPredicate(components, pad + 1, translatable("emi.property.condition.predicate.target", value(translatableType("emi.enum.target", target))), predicate);

        return components;
    }
}
