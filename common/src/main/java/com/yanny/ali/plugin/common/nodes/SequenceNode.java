package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class SequenceNode extends CompositeNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "sequence");

    private final List<ITooltipNode> tooltip;

    public SequenceNode(List<ILootModifier<?>> modifiers, IServerUtils utils, SequentialEntry entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        super(modifiers, utils, entry, chance, sumWeight, functions, conditions);
        tooltip = EntryTooltipUtils.getSequentialTooltip();
    }

    public SequenceNode(IClientUtils utils, FriendlyByteBuf buf) {
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
