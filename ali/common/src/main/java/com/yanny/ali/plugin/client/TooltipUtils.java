package com.yanny.ali.plugin.client;

import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.number.NumberFormatter;
import com.yanny.aci.tooltip.NumberOptions;
import com.yanny.aci.tooltip.TooltipStyle;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TooltipUtils {
    @NotNull
    public static TooltipStyle getStyle() {
        AliClientRegistry registry = PluginManager.getInstance().clientRegistry;

        if (registry == null) {
            return TooltipStyle.DEFAULT;
        }

        return registry.getConfiguration().tooltipColors.resolve(Utils.MOD_ID);
    }

    @NotNull
    public static NumberOptions getNumberOptions() {
        AliClientRegistry registry = PluginManager.getInstance().clientRegistry;

        if (registry == null) {
            return NumberOptions.client(false);
        }

        return NumberOptions.client(registry.getConfiguration().showCharts);
    }

    @NotNull
    public static NumberInterval getSlotCount(IItemNode node) {
        NumberInterval bounds = NumberFormatter.slotBounds(node.getCount());
        NumberInterval limit = NodeUtils.getCountLimit(node.getItem());

        return limit != null ? bounds.clamp(limit.lo(), limit.hi()) : bounds;
    }

    @NotNull
    public static IDataNode getDisplayedOption(List<IDataNode> options, ItemStack displayed) {
        for (IDataNode option : options) {
            if (((IItemNode) option).getItems().stream().anyMatch((s) -> ItemStack.isSameItemSameComponents(s, displayed))) {
                return option;
            }
        }

        return options.get(0);
    }
}
