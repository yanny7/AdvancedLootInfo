package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.stream.Stream;

public class LootPoolNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "loot_pool");

    private final ITooltipNode tooltip;

    public LootPoolNode(IServerUtils utils, LootPool lootPool, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), lootPool.functions.stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), lootPool.conditions.stream()).toList();
        int sumWeight = NodeUtils.getTotalWeight(lootPool.entries);

        tooltip = EntryTooltipUtils.getLootPoolTooltip(utils.convertNumber(utils, lootPool.rolls), utils.convertNumber(utils, lootPool.bonusRolls));

        for (LootPoolEntryContainer entry : lootPool.entries) {
            addChildren(utils.getEntryFactory(utils, entry).create(utils, entry, chance, sumWeight, allFunctions, allConditions));
        }
    }

    public LootPoolNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = ITooltipNode.decodeNode(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
