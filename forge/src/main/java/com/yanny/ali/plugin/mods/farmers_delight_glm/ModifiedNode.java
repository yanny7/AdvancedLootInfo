package com.yanny.ali.plugin.mods.farmers_delight_glm;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ModifiedNode extends ListNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "modified");

    private final ITooltipNode tooltip;

    public ModifiedNode(IServerUtils utils, IDataNode original, IDataNode modified) {
        tooltip = EntryTooltipUtils.getAlternativesTooltip();
        addChildren(modified);
        addChildren(original);
    }

    public ModifiedNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
    public ResourceLocation getId() {
        return ID;
    }
}
