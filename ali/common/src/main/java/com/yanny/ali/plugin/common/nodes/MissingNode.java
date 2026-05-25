package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class MissingNode implements IDataNode {
    public static final Identifier ID = Utils.modLoc("missing");

    private final TooltipNode tooltip;

    public MissingNode(TooltipNode tooltip) {
        this.tooltip = tooltip;
    }

    public MissingNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
