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

import java.util.ArrayList;
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
        List<ILootModifier<?>> filteredModifiers = new ArrayList<>();
        List<ILootModifier<?>> addModifiers = new ArrayList<>();
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), Arrays.stream(lootTable.functions)).toList();

        for (ILootModifier<?> modifier : modifiers) {
            if (modifier.getOperation() == ILootModifier.Operation.ADD) {
                addModifiers.add(modifier);
            } else {
                filteredModifiers.add(modifier);
            }
        }

        tooltip = EntryTooltipUtils.getLootTableTooltip();

        for (LootPool lootPool : utils.getLootPools(lootTable)) {
            addChildren(filteredModifiers, new LootPoolNode(filteredModifiers, utils, lootPool, chance, allFunctions, conditions));
        }

        for (ILootModifier<?> modifier : addModifiers) {
            if (modifier.getOperation() == ILootModifier.Operation.ADD) {
                addChildren(Collections.emptyList(), modifier.getNode());
            }
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
