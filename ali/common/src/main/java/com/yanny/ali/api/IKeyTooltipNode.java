package com.yanny.ali.api;

public interface IKeyTooltipNode {
    IKeyTooltipNode add(ITooltipNode node);

    ITooltipNode build(String key);
}
