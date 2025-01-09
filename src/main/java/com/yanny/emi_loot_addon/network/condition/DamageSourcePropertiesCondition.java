package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinDamageSourceCondition;
import com.yanny.emi_loot_addon.network.LootCondition;
import com.yanny.emi_loot_addon.network.TooltipUtils;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.translatableType;

public class DamageSourcePropertiesCondition extends LootCondition {
    public final DamageSourcePredicate predicate;

    public DamageSourcePropertiesCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        predicate = ((MixinDamageSourceCondition) condition).getPredicate();
    }

    public DamageSourcePropertiesCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        predicate = DamageSourcePredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        TooltipUtils.addDamageSourcePredicate(components, pad, translatableType("emi.type.emi_loot_addon.condition", type), predicate);

        return components;
    }
}
