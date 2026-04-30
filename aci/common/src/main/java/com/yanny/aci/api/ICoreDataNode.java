package com.yanny.aci.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface ICoreDataNode<
        TServerUtils extends ICoreServerUtils<?, ?, ?>,
        TTooltipNode extends ICoreTooltipNode<?>
        > extends Comparable<ICoreDataNode<?, ?>> {
    @NotNull
    TTooltipNode getTooltip();

    @NotNull
    ResourceLocation getId();

    void encode(TServerUtils utils, RegistryFriendlyByteBuf buf);

    default float getChance() {
        return 1;
    }

    @Override
    default int compareTo(ICoreDataNode<?, ?> o) {
        return Float.compare(o.getChance(), getChance());
    }
}
