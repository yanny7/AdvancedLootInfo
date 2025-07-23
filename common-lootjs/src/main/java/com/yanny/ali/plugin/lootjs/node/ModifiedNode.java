package com.yanny.ali.plugin.lootjs.node;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.lootjs.LootJsPlugin;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ModifiedNode extends ListNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LootJsPlugin.ID, "modified");

    private final List<ITooltipNode> tooltip;

    public ModifiedNode(IServerUtils utils, IDataNode original, IDataNode modified) {
        tooltip = EntryTooltipUtils.getAlternativesTooltip();
        addChildren(modified);
        addChildren(original);
    }

    public ModifiedNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
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
