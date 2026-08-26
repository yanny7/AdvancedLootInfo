package com.yanny.awi.plugin.client;

import com.yanny.aci.tooltip.TooltipStyle;
import com.yanny.awi.Utils;
import com.yanny.awi.manager.AwiClientRegistry;
import com.yanny.awi.manager.PluginManager;
import org.jetbrains.annotations.NotNull;

public class TooltipUtils {
    @NotNull
    public static TooltipStyle getStyle() {
        AwiClientRegistry registry = PluginManager.getInstance().clientRegistry;

        if (registry == null) {
            return TooltipStyle.DEFAULT;
        }

        return registry.getConfiguration().tooltipColors.resolve(Utils.MOD_ID);
    }
}
