package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LootPoolNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "loot_pool");

    private final List<ITooltipNode> tooltip;

    public LootPoolNode(List<ILootModifier<?>> modifiers, IServerUtils utils, LootPool lootPool, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), Arrays.stream(lootPool.functions)).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), Arrays.stream(lootPool.conditions)).toList();
        int sumWeight = NodeUtils.getTotalWeight(Arrays.asList(lootPool.entries));

        tooltip = EntryTooltipUtils.getLootPoolTooltip(utils.convertNumber(utils, lootPool.rolls), utils.convertNumber(utils, lootPool.bonusRolls));

        for (LootPoolEntryContainer entry : lootPool.entries) {
            addChildren(modifiers, utils.getEntryFactory(utils, entry).create(modifiers, utils, entry, chance, sumWeight, allFunctions, allConditions));
        }
    }

    public LootPoolNode(IClientUtils utils, FriendlyByteBuf buf) {
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
