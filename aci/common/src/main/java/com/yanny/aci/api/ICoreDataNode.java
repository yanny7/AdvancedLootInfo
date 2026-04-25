package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface ICoreDataNode
        <
                SU extends ICoreServerUtils,
                TN extends ICoreTooltipNode<SU>
        >
        extends Comparable<ICoreDataNode<SU, TN>> {
    @NotNull
    TN getTooltip();

    @NotNull
    ResourceLocation getId();

    void encode(SU utils, RegistryFriendlyByteBuf buf);

    default float getChance() {
        return 1;
    }

    @Override
    default int compareTo(ICoreDataNode o) {
        return Float.compare(o.getChance(), getChance());
    }
}
