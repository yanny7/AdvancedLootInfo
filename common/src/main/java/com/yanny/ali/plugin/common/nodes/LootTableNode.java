package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class LootTableNode extends ListNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "loot_table");

    private final ITooltipNode tooltip;

    public LootTableNode(List<ILootModifier<?>> modifiers, IServerUtils utils, LootTable lootTable) {
        this(modifiers, utils, lootTable, 1, Collections.emptyList(), Collections.emptyList());
    }

    public LootTableNode(IServerUtils utils, LootTable lootTable, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this(Collections.emptyList(), utils, lootTable, chance, functions, conditions);
    }

    public LootTableNode(List<ILootModifier<?>> modifiers, IServerUtils utils, LootTable lootTable, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), lootTable.functions.stream()).toList();

        tooltip = EntryTooltipUtils.getLootTableTooltip();

        for (LootPool lootPool : lootTable.pools) {
            addChildren(new LootPoolNode(Collections.emptyList(), utils, lootPool, chance, allFunctions, conditions));
        }

        for (ILootModifier<?> modifier : modifiers) {
            NodeUtils.processLootModifier(utils, modifier, this);
        }
    }

    public LootTableNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
