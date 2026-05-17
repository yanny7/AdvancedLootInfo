package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LootTableNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("loot_table");

    private final TooltipNode tooltip;

    public LootTableNode(List<IDataNode> children, TooltipNode tooltip) {
        children.forEach(this::addChildren);
        this.tooltip = tooltip;
    }

    public LootTableNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.decode(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        tooltip.encode(utils, buf);
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }
}
