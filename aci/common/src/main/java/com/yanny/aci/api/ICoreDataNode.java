package com.yanny.aci.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface ICoreDataNode<SU extends ICoreServerUtils, TN extends ICoreTooltipNode<SU>> extends Comparable<ICoreDataNode<SU, TN>> {
    TN getTooltip();

    ResourceLocation getId();

    void encode(SU utils, FriendlyByteBuf buf);

    default float getChance() {
        return 1;
    }

    @Override
    default int compareTo(ICoreDataNode o) {
        return Float.compare(o.getChance(), getChance());
    }
}
