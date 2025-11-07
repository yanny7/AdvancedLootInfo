package com.yanny.ali.api;

public interface IKeyTooltipNode extends ITooltipNode {
    IKeyTooltipNode key(String key);

    IKeyTooltipNode add(ITooltipNode node);
}
