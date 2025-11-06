package com.yanny.ali.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IDataNode extends Comparable<IDataNode> {
    ITooltipNode getTooltip();

    ResourceLocation getId();

    void encode(IServerUtils utils, FriendlyByteBuf buf);

    default float getChance() {
        return 1;
    }

    @Override
    default int compareTo(IDataNode o) {
        return Float.compare(o.getChance(), getChance());
    }
}
