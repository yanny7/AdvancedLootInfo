package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.awi.plugin.common.tooltip.ValueTooltipNode;

import java.lang.reflect.Array;

public class TooltipUtils {

    public static IKeyTooltipNode getArrayTooltip(IServerUtils utils, Object value) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch();
        int length = Array.getLength(value);

        if (length > 0) {
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);

                tooltip.add(utils.buildTooltip(utils.getValueTooltip(utils, element)));
            }
        } else {
            return ValueTooltipNode.value("[]");
        }

        return tooltip;
    }
}
