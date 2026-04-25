package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ModifiedNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("modified");

    private final ITooltipNode tooltip;

    public ModifiedNode(IServerUtils ignoredUtils, IDataNode original, IDataNode modified) {
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

    @NotNull
    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }
}
