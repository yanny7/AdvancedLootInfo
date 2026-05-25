package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class GlobalLootModifierNode implements IDataNode {
    public static final Identifier ID = Utils.modLoc("glm");

    private final TooltipNode tooltip;

    public GlobalLootModifierNode(TooltipNode tooltip) {
        this.tooltip = tooltip;
    }

    public GlobalLootModifierNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        tooltip = TooltipNode.CACHE.getNodeById(buf.readVarInt());
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

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(TooltipNode.CACHE.getNodeId(tooltip));
    }
}
