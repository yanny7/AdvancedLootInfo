package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class EmptyNode implements IDataNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "empty");

    private final List<ITooltipNode> tooltip;

    public EmptyNode(List<ILootModifier<?>> modifiers, IServerUtils utils, EmptyLootItem entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        tooltip = EntryTooltipUtils.getEmptyTooltip(utils, entry, chance, sumWeight, functions, conditions);
    }

    public EmptyNode(IClientUtils utils, FriendlyByteBuf buf) {
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
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
