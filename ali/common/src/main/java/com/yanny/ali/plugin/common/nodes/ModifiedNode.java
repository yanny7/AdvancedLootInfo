package com.yanny.ali.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ModifiedNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("modified");

    private final TooltipNode tooltip;

    public ModifiedNode(IServerUtils ignoredUtils, IDataNode original, IDataNode modified) {
        tooltip = EntryTooltipUtils.getAlternativesTooltip().build();
        addChildren(modified);
        addChildren(original);
    }

    public ModifiedNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
