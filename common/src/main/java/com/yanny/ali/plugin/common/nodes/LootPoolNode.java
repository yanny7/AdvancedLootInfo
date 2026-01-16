package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.List;

public class LootPoolNode extends ListNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "loot_pool");

    private final ITooltipNode tooltip;

    public LootPoolNode(List<IDataNode> children, ITooltipNode tooltip) {
        children.forEach(this::addChildren);
        this.tooltip = tooltip;
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
    public Identifier getId() {
        return ID;
    }
}
