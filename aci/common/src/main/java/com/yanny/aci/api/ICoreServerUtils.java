package com.yanny.aci.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICoreServerUtils<
        TKeyTooltipNode extends ICoreKeyTooltipNode<?, ?>,
        TTooltipNode    extends ICoreTooltipNode<?>,
        SELF            extends ICoreServerUtils<?, ?, ?>
        > {
    @NotNull
    <T> TKeyTooltipNode getValueTooltip(SELF utils, @Nullable T value);

    @Nullable
    ServerLevel getServerLevel();

    @NotNull
    TTooltipNode buildTooltip(TKeyTooltipNode keyTooltipNode);

    @NotNull
    TKeyTooltipNode getBranchNode();

    @NotNull
    TKeyTooltipNode getBranchNode(boolean isAdvancedTooltip);

    @NotNull
    TKeyTooltipNode getValueNode(Object ...value);

    @NotNull
    TKeyTooltipNode getComponentNode(Component ...values);

    @NotNull
    TKeyTooltipNode getEmptyNode();
}
