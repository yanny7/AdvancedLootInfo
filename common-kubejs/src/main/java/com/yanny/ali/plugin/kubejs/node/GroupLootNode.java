package com.yanny.ali.plugin.kubejs.node;

import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.loot.action.AddLootAction;
import com.almostreliable.lootjs.loot.action.GroupedLootAction;
import com.almostreliable.lootjs.loot.action.WeightedAddLootAction;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinCompositeLootAction;
import com.yanny.ali.mixin.MixinGroupedLootAction;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class GroupLootNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "grouped_loot");

    private final List<ITooltipNode> tooltip;

    public GroupLootNode(IServerUtils utils, GroupedLootAction lootPool, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        MixinGroupedLootAction action = (MixinGroupedLootAction) lootPool;

        tooltip = EntryTooltipUtils.getLootPoolTooltip(utils.convertNumber(utils, action.getNumberProvider()), new RangeValue(0));

        for (ILootHandler entry : ((MixinCompositeLootAction) lootPool).getHandlers()) {
            if (entry instanceof AddLootAction addLootAction) {
                addChildren(new AddLootNode(utils, addLootAction, functions, conditions));
            } else if (entry instanceof WeightedAddLootAction weightedAddLootNode) {
                addChildren(new WeightedAddLootNode(utils, weightedAddLootNode, functions, conditions));
            }
        }
    }

    public GroupLootNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
