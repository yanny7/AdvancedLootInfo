package com.yanny.ali.plugin.client;

import com.yanny.aci.tooltip.TooltipStyle;
import com.yanny.ali.Utils;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import org.jetbrains.annotations.NotNull;

public class TooltipUtils {
    @NotNull
    public static TooltipStyle getStyle() {
        AliClientRegistry registry = PluginManager.getInstance().clientRegistry;

        if (registry == null) {
            return TooltipStyle.DEFAULT;
        }

        return registry.getConfiguration().tooltipColors.resolve(Utils.MOD_ID);
    }
}
