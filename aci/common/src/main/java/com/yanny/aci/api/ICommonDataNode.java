package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface ICommonDataNode<SU extends ICommonServerUtils, TN extends ICommonTooltipNode<SU>> extends Comparable<ICommonDataNode<SU, TN>> {
    TN getTooltip();

    ResourceLocation getId();

    void encode(SU utils, RegistryFriendlyByteBuf buf);

    default float getChance() {
        return 1;
    }

    @Override
    default int compareTo(ICommonDataNode o) {
        return Float.compare(o.getChance(), getChance());
    }
}
