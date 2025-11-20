package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class GlobalLootModifierNode implements IDataNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "glm");

    private final ITooltipNode tooltip;

    public GlobalLootModifierNode(IServerUtils utils, ITooltipNode tooltip) {
        this.tooltip = tooltip;
    }

    public GlobalLootModifierNode(IClientUtils utils, FriendlyByteBuf buf) {
        tooltip = ITooltipNode.decodeNode(utils, buf);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
    }
}
