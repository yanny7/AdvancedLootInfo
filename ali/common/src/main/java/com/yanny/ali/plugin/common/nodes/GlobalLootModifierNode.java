package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GlobalLootModifierNode implements IDataNode {
    public static final ResourceLocation ID = Utils.modLoc("glm");

    private final ITooltipNode tooltip;

    public GlobalLootModifierNode(ITooltipNode tooltip) {
        this.tooltip = tooltip;
    }

    public GlobalLootModifierNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        tooltip = ITooltipNode.decodeNode(utils, buf);
    }

    @NotNull
    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
    }
}
