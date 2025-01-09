package com.yanny.advanced_loot_info.network.condition;

import com.yanny.advanced_loot_info.mixin.MixinMatchTool;
import com.yanny.advanced_loot_info.network.LootCondition;
import com.yanny.advanced_loot_info.network.TooltipUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatableType;

public class MatchToolCondition extends LootCondition {
    public ItemPredicate predicate;

    public MatchToolCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        predicate = ((MixinMatchTool) condition).getPredicate();
    }

    public MatchToolCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        predicate = ItemPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        TooltipUtils.addItemPredicate(components, pad + 1, translatableType("emi.type.advanced_loot_info.condition", type), predicate);

        return components;
    }
}
