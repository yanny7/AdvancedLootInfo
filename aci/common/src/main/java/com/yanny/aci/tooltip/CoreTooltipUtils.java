package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CoreTooltipUtils {
    @NotNull
    public static List<Component> toComponents(HolderLookup.Provider provider, List<TooltipNode> tooltip, int pad, boolean showAdvancedTooltip) {
        List<Component> components = new ArrayList<>();

        for (TooltipNode node : tooltip) {
            components.addAll(toComponents(provider, node, pad, showAdvancedTooltip));
        }

        return components;
    }

    @NotNull
    public static List<Component> toComponents(HolderLookup.Provider provider, TooltipNode tooltip, int pad, boolean showAdvancedTooltip) {
        return tooltip.getComponents(provider, pad, showAdvancedTooltip);
    }

    @NotNull
    public static <
            T,
            TServerUtils extends ICoreServerUtils<TServerUtils>
            > TooltipBuilder getBuiltInRegistryTooltip(TServerUtils utils, Registry<T> registry, T value) {
        return utils.getValueTooltip(utils, registry.getKey(value));
    }
}
