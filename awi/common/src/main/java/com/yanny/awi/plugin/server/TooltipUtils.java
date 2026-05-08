package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;

import java.lang.reflect.Array;

public class TooltipUtils {

    public static TooltipBuilder getArrayTooltip(IServerUtils utils, Object value) {
        int length = Array.getLength(value);

        if (length > 0) {
            return TooltipBuilder.array((b) -> {
                for (int i = 0; i < length; i++) {
                    b.add(utils.getValueTooltip(utils, Array.get(value, i)));
                }
            });
        } else {
            return TooltipBuilder.value("[]");
        }
    }
}
