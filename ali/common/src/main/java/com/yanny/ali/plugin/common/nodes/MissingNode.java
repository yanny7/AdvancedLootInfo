package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MissingNode implements IDataNode {
    public static final ResourceLocation ID = Utils.modLoc("missing");

    private final TooltipNode tooltip;

    public MissingNode(TooltipNode tooltip) {
        this.tooltip = tooltip;
    }

    public MissingNode(IClientUtils utils, FriendlyByteBuf buf) {
        tooltip = TooltipNode.decode(buf);
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        tooltip.encode(buf);
    }
}
