package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlternativesNode extends CompositeNode {
    public static final Identifier ID = Utils.modLoc("alternatives");

    private final TooltipNode tooltip;

    public AlternativesNode(List<IDataNode> children, TooltipNode tooltip) {
        super(children);
        this.tooltip = tooltip;
    }

    public AlternativesNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.CACHE.getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(TooltipNode.CACHE.getNodeId(tooltip));
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
