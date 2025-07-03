package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class LootTableNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "loot_table");

    private final List<ITooltipNode> tooltip;

    public LootTableNode(List<ILootModifier<?>> modifiers, IServerUtils utils, LootTable lootTable) {
        this(modifiers, utils, lootTable, 1, Collections.emptyList(), Collections.emptyList());
    }

    public LootTableNode(List<ILootModifier<?>> modifiers, IServerUtils utils, LootTable lootTable, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), Arrays.stream(lootTable.functions)).toList();

        tooltip = EntryTooltipUtils.getLootTableTooltip();

        for (LootPool lootPool : utils.getLootPools(lootTable)) {
            addChildren(new LootPoolNode(Collections.emptyList(), utils, lootPool, chance, allFunctions, conditions));
        }

        for (ILootModifier<?> modifier : modifiers) {
            NodeUtils.processLootModifier(modifier, this);
        }
    }

    public LootTableNode(IClientUtils utils, FriendlyByteBuf buf) {
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
