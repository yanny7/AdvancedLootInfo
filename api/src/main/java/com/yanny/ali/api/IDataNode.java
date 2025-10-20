package com.yanny.ali.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IDataNode extends Comparable<IDataNode> {
    List<ITooltipNode> getTooltip();

    ResourceLocation getId();

    void encode(IServerUtils utils, RegistryFriendlyByteBuf buf);

    default float getChance() {
        return 1;
    }

    @Override
    default int compareTo(IDataNode o) {
        return Float.compare(o.getChance(), getChance());
    }
}
