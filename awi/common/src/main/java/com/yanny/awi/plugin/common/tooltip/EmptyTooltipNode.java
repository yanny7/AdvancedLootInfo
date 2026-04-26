package com.yanny.awi.plugin.common.tooltip;

import com.yanny.aci.tooltip.CoreEmptyTooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EmptyTooltipNode extends CoreEmptyTooltipNode<IServerUtils, ITooltipNode, IKeyTooltipNode> implements ITooltipNode, IKeyTooltipNode {
    public static final EmptyTooltipNode EMPTY = new EmptyTooltipNode();
    public static final ResourceLocation ID = Utils.modLoc("empty");

    private EmptyTooltipNode() {}

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static Builder empty() {
        return new Builder();
    }

    public static EmptyTooltipNode decode(IClientUtils ignoredUtils, FriendlyByteBuf ignoredBuf) {
        return EMPTY;
    }

    public static class Builder extends CoreEmptyTooltipNode.Builder<IServerUtils, ITooltipNode, IKeyTooltipNode> implements IKeyTooltipNode {
        public Builder() {
            super(EMPTY);
        }
    }
}
