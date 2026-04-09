package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.List;

public class GroupNode extends CompositeNode {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Utils.MOD_ID, "group");

    private final ITooltipNode tooltip;

    public GroupNode(List<IDataNode> children, ITooltipNode tooltip) {
        super(children);
        this.tooltip = tooltip;
    }

    public GroupNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
