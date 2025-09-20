package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;

public interface IIngredientTooltip {
    ITooltipNode getTooltip(IServerUtils utils);
}
