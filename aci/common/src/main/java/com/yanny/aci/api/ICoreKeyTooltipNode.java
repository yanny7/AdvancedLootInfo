package com.yanny.aci.api;

import org.jetbrains.annotations.NotNull;

public interface ICoreKeyTooltipNode<
        TTooltipNode extends ICoreTooltipNode<?>,
        SELF         extends ICoreKeyTooltipNode<?, ?>
        > {
    @NotNull
    SELF add(TTooltipNode node);

    @NotNull
    TTooltipNode build(String key);
}
