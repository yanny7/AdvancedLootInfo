package com.yanny.ali.plugin.condition;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.mixin.MixinMatchTool;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class MatchToolCondition implements ILootCondition {
    public final ItemPredicate predicate;

    public MatchToolCondition(IContext context, LootItemCondition condition) {
        predicate = ((MixinMatchTool) condition).getPredicate();
    }

    public MatchToolCondition(IContext context, FriendlyByteBuf buf) {
        predicate = ItemPredicate.fromJson(buf.readJsonWithCodec(ExtraCodecs.JSON));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ExtraCodecs.JSON, predicate.serializeToJson());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        TooltipUtils.addItemPredicate(components, pad + 1, translatable("ali.type.condition.match_tool"), predicate);

        return components;
    }
}
