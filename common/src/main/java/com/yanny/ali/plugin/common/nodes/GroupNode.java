package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class GroupNode extends CompositeNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "group");

    private final ITooltipNode tooltip;

    public GroupNode(IServerUtils utils, EntryGroup entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        super(utils, entry, chance, sumWeight, functions, conditions);
        tooltip = EntryTooltipUtils.getGroupTooltip();
    }

    public GroupNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.decodeNode(buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        TooltipNode.encodeNode(tooltip, buf);
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
