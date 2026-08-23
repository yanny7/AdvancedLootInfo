package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CoreTooltipUtils {
    /** @deprecated use {@link #toComponents(List, int, boolean, TooltipStyle)} */
    @Deprecated(forRemoval = true, since = "1.1.0")
    @NotNull
    public static List<Component> toComponents(List<TooltipNode> tooltip, int pad, boolean showAdvancedTooltip) {
        return toComponents(tooltip, pad, showAdvancedTooltip, TooltipStyle.DEFAULT);
    }

    @NotNull
    public static List<Component> toComponents(List<TooltipNode> tooltip, int pad, boolean showAdvancedTooltip, TooltipStyle style) {
        List<Component> components = new ArrayList<>();

        for (TooltipNode node : tooltip) {
            components.addAll(toComponents(node, pad, showAdvancedTooltip, style));
        }

        return components;
    }

    /** @deprecated use {@link #toComponents(TooltipNode, int, boolean, TooltipStyle)} */
    @Deprecated(forRemoval = true, since = "1.1.0")
    @NotNull
    public static List<Component> toComponents(TooltipNode tooltip, int pad, boolean showAdvancedTooltip) {
        return toComponents(tooltip, pad, showAdvancedTooltip, TooltipStyle.DEFAULT);
    }

    @NotNull
    public static List<Component> toComponents(TooltipNode tooltip, int pad, boolean showAdvancedTooltip, TooltipStyle style) {
        return tooltip.getComponents(pad, showAdvancedTooltip, style);
    }

    @NotNull
    public static <
            T,
            TServerUtils extends ICoreServerUtils<TServerUtils>
            > TooltipBuilder getBuiltInRegistryTooltip(TServerUtils utils, Registry<T> registry, T value) {
        return utils.getValueTooltip(utils, registry.getKey(value));
    }
}
